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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testfx.framework.junit5.Start;
import ua.org.java.dynamoit.DynamoItApp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class for DynamoIt End-to-End tests.
 * Sets up DynamoDB Local in a Docker container and provides test utilities.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DynamoItE2ETestBase {

    @Container
    protected static final GenericContainer<?> DYNAMO_DB_LOCAL = new GenericContainer<>("amazon/dynamodb-local:latest")
            .withExposedPorts(8000)
            .withCommand("-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory");

    protected AmazonDynamoDB dynamoDbClient;
    protected String dynamoDbEndpoint;

    @BeforeAll
    void setUpDynamoDb() throws Exception {
        // Start container if not already started
        if (!DYNAMO_DB_LOCAL.isRunning()) {
            DYNAMO_DB_LOCAL.start();
        }

        dynamoDbEndpoint = "http://" + DYNAMO_DB_LOCAL.getHost() + ":" + DYNAMO_DB_LOCAL.getFirstMappedPort();
        
        // Create DynamoDB client for test setup
        dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("fake", "fake")))
                .build();

        // Set system property for DynamoIt to use local endpoint
        System.setProperty("aws.dynamodb.endpoint", dynamoDbEndpoint);
        System.setProperty("aws.accessKeyId", "fake");
        System.setProperty("aws.secretAccessKey", "fake");
        System.setProperty("aws.region", "us-east-1");

        // Create test tables and data
        createTestTablesAndData();
    }

    @AfterAll
    void tearDownDynamoDb() {
        if (dynamoDbClient != null) {
            // Clean up tables
            cleanupTestTables();
            dynamoDbClient.shutdown();
        }
    }

    @Start
    void startApplication(Stage stage) throws Exception {
        // Ensure JavaFX is initialized properly
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                new DynamoItApp().start(stage);
                latch.countDown();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start DynamoIt application", e);
            }
        });
        
        // Wait for application to start with timeout
        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("Application failed to start within 30 seconds");
        }
        
        // Give the UI a moment to fully initialize
        Thread.sleep(2000);
    }

    /**
     * Creates test tables and populates them with sample data
     */
    private void createTestTablesAndData() {
        // Create Users table
        createUsersTable();
        populateUsersTable();
        
        // Create Products table
        createProductsTable();
        populateProductsTable();
    }

    private void createUsersTable() {
        CreateTableRequest createUsersTable = new CreateTableRequest()
                .withTableName("users")
                .withKeySchema(
                        new KeySchemaElement("id", KeyType.HASH)
                )
                .withAttributeDefinitions(
                        new AttributeDefinition("id", ScalarAttributeType.S)
                )
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        try {
            dynamoDbClient.createTable(createUsersTable);
            waitForTableToBeActive("users");
        } catch (ResourceInUseException e) {
            // Table already exists, ignore
        }
    }

    private void populateUsersTable() {
        Map<String, AttributeValue> user1 = new HashMap<>();
        user1.put("id", new AttributeValue("1"));
        user1.put("name", new AttributeValue("Alice"));
        user1.put("email", new AttributeValue("alice@example.com"));
        user1.put("age", new AttributeValue().withN("30"));

        Map<String, AttributeValue> user2 = new HashMap<>();
        user2.put("id", new AttributeValue("2"));
        user2.put("name", new AttributeValue("Bob"));
        user2.put("email", new AttributeValue("bob@example.com"));
        user2.put("age", new AttributeValue().withN("25"));

        Map<String, AttributeValue> user3 = new HashMap<>();
        user3.put("id", new AttributeValue("3"));
        user3.put("name", new AttributeValue("Charlie"));
        user3.put("email", new AttributeValue("charlie@example.com"));
        user3.put("age", new AttributeValue().withN("35"));

        dynamoDbClient.putItem("users", user1);
        dynamoDbClient.putItem("users", user2);
        dynamoDbClient.putItem("users", user3);
    }

    private void createProductsTable() {
        CreateTableRequest createProductsTable = new CreateTableRequest()
                .withTableName("products")
                .withKeySchema(
                        new KeySchemaElement("productId", KeyType.HASH),
                        new KeySchemaElement("category", KeyType.RANGE)
                )
                .withAttributeDefinitions(
                        new AttributeDefinition("productId", ScalarAttributeType.S),
                        new AttributeDefinition("category", ScalarAttributeType.S)
                )
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        try {
            dynamoDbClient.createTable(createProductsTable);
            waitForTableToBeActive("products");
        } catch (ResourceInUseException e) {
            // Table already exists, ignore
        }
    }

    private void populateProductsTable() {
        Map<String, AttributeValue> product1 = new HashMap<>();
        product1.put("productId", new AttributeValue("PROD-001"));
        product1.put("category", new AttributeValue("Electronics"));
        product1.put("name", new AttributeValue("Laptop"));
        product1.put("price", new AttributeValue().withN("999.99"));

        Map<String, AttributeValue> product2 = new HashMap<>();
        product2.put("productId", new AttributeValue("PROD-002"));
        product2.put("category", new AttributeValue("Books"));
        product2.put("name", new AttributeValue("Java Programming"));
        product2.put("price", new AttributeValue().withN("49.99"));

        dynamoDbClient.putItem("products", product1);
        dynamoDbClient.putItem("products", product2);
    }

    private void waitForTableToBeActive(String tableName) {
        try {
            while (true) {
                DescribeTableResult result = dynamoDbClient.describeTable(tableName);
                if (TableStatus.ACTIVE.toString().equals(result.getTable().getTableStatus())) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for table to be active", e);
        }
    }

    private void cleanupTestTables() {
        try {
            dynamoDbClient.deleteTable("users");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        try {
            dynamoDbClient.deleteTable("products");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Utility method to wait for UI operations to complete
     */
    protected void waitForUi() throws InterruptedException {
        Thread.sleep(500);
    }

    /**
     * Get an item from the database for verification
     */
    protected Map<String, AttributeValue> getItemFromDb(String tableName, Map<String, AttributeValue> key) {
        GetItemRequest request = new GetItemRequest()
                .withTableName(tableName)
                .withKey(key);
        GetItemResult result = dynamoDbClient.getItem(request);
        return result.getItem();
    }
}
