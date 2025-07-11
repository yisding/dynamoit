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

package ua.org.java.dynamoit.components.tablegrid;

import javafx.application.HostServices;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import ua.org.java.dynamoit.EventBus;
import ua.org.java.dynamoit.components.main.MainModel;
import ua.org.java.dynamoit.db.DynamoDBService;
import ua.org.java.dynamoit.model.TableDef;
import ua.org.java.dynamoit.model.profile.PreconfiguredProfileDetails;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TableGridControllerTest {

    @Test
    public void onRefreshData() {
        TableGridContext context = new TableGridContext(
                new PreconfiguredProfileDetails("profile1", "region1"),
                "table1"
        );
        MainModel mainModel = new MainModel();
        mainModel.addProfile(new PreconfiguredProfileDetails("profile1", "region1"));
        mainModel.addProfile(new PreconfiguredProfileDetails("profile2", "region2"));
        TableDef tableDef = new TableDef("Table1");
        tableDef.setHashAttribute("hash_attr");
        TableGridModel model = new TableGridModel(mainModel.getAvailableProfiles().get("profile1"));
        model.setTableDef(tableDef);
        model.getRows().add(new HashMap<>());
        model.setOriginalTableDescription(TableDescription.builder()
                .tableName("Table1")
                .keySchema(List.of(
                        KeySchemaElement.builder().attributeName("hash_attr").keyType(KeyType.HASH).build()
                ))
                .attributeDefinitions(List.of(
                        AttributeDefinition.builder().attributeName("hash_attr").attributeType(ScalarAttributeType.S).build()
                ))
                .itemCount(0L)
                .globalSecondaryIndexes(Collections.emptyList())  // or null if allowed
                .build());

        DynamoDBService dynamoDBService = mock(DynamoDBService.class);
        DynamoDbClient client = mock(DynamoDbClient.class);
        HostServices hostServices = mock(HostServices.class);
        EventBus eventBus = new EventBus(ForkJoinPool.commonPool());

        TableGridController controller = spy(new TableGridController(context, model, dynamoDBService, eventBus, ForkJoinPool.commonPool(), hostServices));

        when(dynamoDBService.getOrCreateDynamoDbClient(context.profileDetails())).thenReturn(client);

        when(controller.executeQueryOrSearch(null)).thenReturn(CompletableFuture.completedFuture(new Pair<>(List.of(new HashMap<>()), new HashMap<>())));

        controller.onRefreshData().join();

        verify(dynamoDBService).getOrCreateDynamoDbClient(context.profileDetails());
        verify(controller).executeQueryOrSearch(null);

        assertEquals(2, model.getRows().size());
    }
}
