/*
 * This file is part of DynamoIt.
 *
 *     DynamoIt is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     DynamoIt is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with DynamoIt.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.org.java.dynamoit.db;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.profiles.Profile;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import ua.org.java.dynamoit.model.profile.LocalProfileDetails;
import ua.org.java.dynamoit.model.profile.PreconfiguredProfileDetails;
import ua.org.java.dynamoit.model.profile.ProfileDetails;
import ua.org.java.dynamoit.model.profile.RemoteProfileDetails;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ua.org.java.dynamoit.utils.RegionsUtils.ALL_REGIONS;

public class DynamoDBService {

    private final Map<Integer, DynamoDbClient> profileDynamoDBClientMap = new HashMap<>();

    public Stream<ProfileDetails> getAvailableProfiles() {
        Map<String, Profile> profiles = ProfileFile.defaultProfileFile().profiles();
        return profiles.values().stream()
                .map(profile -> new PreconfiguredProfileDetails(profile.name(), profile.property("region").orElse(ALL_REGIONS.get(0))));
    }

    public CompletableFuture<List<String>> getListOfTables(ProfileDetails profileDetails) {
        return CompletableFuture.supplyAsync(() -> {
            DynamoDbClient dbClient = getOrCreateDynamoDbClient(profileDetails);
            String lastEvaluatedTableName = null;
            List<String> tableNames = new ArrayList<>();
            do {
                ListTablesRequest request = ListTablesRequest.builder()
                        .exclusiveStartTableName(lastEvaluatedTableName)
                        .build();
                ListTablesResponse response = dbClient.listTables(request);
                lastEvaluatedTableName = response.lastEvaluatedTableName();
                tableNames.addAll(response.tableNames());
            } while (lastEvaluatedTableName != null);

            return tableNames;
        });
    }

    public DynamoDbClient getOrCreateDynamoDbClient(ProfileDetails profileDetails) {
        return profileDynamoDBClientMap.computeIfAbsent(profileDetails.hashCode(), __ -> {
            if (profileDetails instanceof PreconfiguredProfileDetails p) {
                return DynamoDbClient.builder()
                        .credentialsProvider(ProfileCredentialsProvider.create(p.getName()))
                        .region(Region.of(p.getRegion()))
                        .build();
            } else if (profileDetails instanceof LocalProfileDetails p) {
                return DynamoDbClient.builder()
                        .endpointOverride(URI.create(p.getEndPoint()))
                        .build();
            } else if (profileDetails instanceof RemoteProfileDetails p) {
                return DynamoDbClient.builder()
                        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(p.getAccessKeyId(), p.getSecretKey())))
                        .region(Region.of(p.getRegion()))
                        .build();
            }
            throw new RuntimeException("That profile details is not supported");
        });
    }

}
