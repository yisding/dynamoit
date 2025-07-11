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

package ua.org.java.dynamoit.e2e;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test to verify the E2E test infrastructure is working correctly.
 */
class SmokeE2ETest extends DynamoItE2ETestBase {

    @Test
    void shouldStartDynamoDbLocalContainer() {
        // Verify that the DynamoDB Local container is running
        assertThat(DYNAMO_DB_LOCAL.isRunning()).isTrue();
        assertThat(dynamoDbEndpoint).isNotNull();
        assertThat(dynamoDbEndpoint).startsWith("http://");
    }

    @Test
    void shouldCreateAndQueryTestTables() {
        // Verify that test tables exist and contain data
        assertThat(dynamoDbClient).isNotNull();
        
        // Query users table
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("1"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Alice");
        assertThat(item.get("email").getS()).isEqualTo("alice@example.com");
        assertThat(item.get("age").getN()).isEqualTo("30");
    }

    @Test
    void shouldHaveSystemPropertiesSet() {
        // Verify that system properties for DynamoIt are set correctly
        assertThat(System.getProperty("aws.dynamodb.endpoint")).isEqualTo(dynamoDbEndpoint);
        assertThat(System.getProperty("aws.accessKeyId")).isEqualTo("fake");
        assertThat(System.getProperty("aws.secretAccessKey")).isEqualTo("fake");
        assertThat(System.getProperty("aws.region")).isEqualTo("us-east-1");
    }
}
