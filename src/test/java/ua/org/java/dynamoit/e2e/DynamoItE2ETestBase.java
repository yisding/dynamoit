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
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testfx.framework.junit5.ApplicationTest;
import ua.org.java.dynamoit.DynamoItApp;
import ua.org.java.dynamoit.e2e.containers.DynamoDbSingletonContainer;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for DynamoIt End-to-End tests.
 * Sets up DynamoDB Local in a Docker container and provides test utilities.
 * Uses a singleton container pattern for improved performance.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DynamoItE2ETestBase extends ApplicationTest {

    protected String testProfileName = "test-local-" + this.getClass().getSimpleName();

    protected AmazonDynamoDB dynamoDbClient;
    protected String dynamoDbEndpoint;

    @BeforeAll
    void setUpDynamoDb() throws Exception {
        // Verify singleton container is running
        if (!DynamoDbSingletonContainer.isRunning()) {
            throw new RuntimeException("DynamoDB singleton container is not running");
        }
        
        // Wait for the container to be fully ready
        Thread.sleep(2000);

        dynamoDbEndpoint = DynamoDbSingletonContainer.getEndpoint();
        System.out.println("DynamoDB endpoint: " + dynamoDbEndpoint);
        
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

        // Test connection with retries
        testDynamoDbConnection();

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
        
        // Clear system properties
        System.clearProperty("aws.dynamodb.endpoint");
        System.clearProperty("aws.accessKeyId");
        System.clearProperty("aws.secretAccessKey");
        System.clearProperty("aws.region");
        
        System.out.println("DynamoDB client and properties cleaned up");
        // Note: We don't stop the singleton container - it stays running for other test classes
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Check if we're running in visible mode on Mac and configure accordingly
        configureForMacVisibleMode();
        
        // Start the DynamoIt application
        new DynamoItApp().start(stage);
        
        // Give the UI more time to fully initialize and render
        // Longer delay for visible mode to allow manual observation
        boolean isVisible = !Boolean.parseBoolean(System.getProperty("testfx.headless", "true"));
        Thread.sleep(isVisible ? 8000 : 3000);
    }

    /**
     * Configure system properties for optimal Mac visible mode experience
     */
    private void configureForMacVisibleMode() {
        boolean isVisible = !Boolean.parseBoolean(System.getProperty("testfx.headless", "true"));
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        
        if (isVisible && isMac) {
            System.out.println("Configuring for Mac visible mode...");
            
            // Ensure we're not forcing headless mode
            System.setProperty("java.awt.headless", "false");
            System.setProperty("testfx.headless", "false");
            
            // Use native Glass platform for better Mac compatibility
            System.setProperty("glass.platform", "");
            System.setProperty("monocle.platform", "");
            
            // Set robot for better Mac interaction
            System.setProperty("testfx.robot", "glass");
            
            // Slower interaction speeds for visible debugging
            System.setProperty("testfx.robot.write_sleep", "300");
            System.setProperty("testfx.robot.key_sleep", "300");
            
            // Enable verbose output for debugging
            System.setProperty("prism.verbose", "true");
            
            // Ensure proper display on Mac
            System.setProperty("apple.awt.application.name", "DynamoIt E2E Test");
            System.setProperty("apple.laf.useScreenMenuBar", "false");
            
            System.out.println("Mac visible mode configuration complete");
        }
    }

    /**
     * Creates test tables and populates them with sample data
     */
    protected void createTestTablesAndData() {
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
     * Automatically adjusts timing based on visible/headless mode
     */
    protected void waitForUi() throws InterruptedException {
        boolean isVisible = !Boolean.parseBoolean(System.getProperty("testfx.headless", "true"));
        Thread.sleep(isVisible ? 2000 : 1000);  // Longer waits in visible mode
    }

    /**
     * Wait for UI with custom timeout
     */
    protected void waitForUi(long milliseconds) throws InterruptedException {
        boolean isVisible = !Boolean.parseBoolean(System.getProperty("testfx.headless", "true"));
        // In visible mode, extend the wait time for better observation
        long actualWait = isVisible ? (long)(milliseconds * 1.5) : milliseconds;
        Thread.sleep(actualWait);
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

    /**
     * Put an item directly into the database for test setup
     */
    protected void putItemInDb(String tableName, Map<String, AttributeValue> item) {
        try {
            dynamoDbClient.putItem(tableName, item);
        } catch (Exception e) {
            // Ignore errors in test setup
        }
    }

    /**
     * Delete an item directly from the database for test cleanup
     */
    protected void deleteItemFromDb(String tableName, Map<String, AttributeValue> key) {
        try {
            DeleteItemRequest request = new DeleteItemRequest()
                    .withTableName(tableName)
                    .withKey(key);
            dynamoDbClient.deleteItem(request);
        } catch (Exception e) {
            // Ignore errors in test cleanup
        }
    }

    /**
     * Get all items from a table (for verification)
     */
    protected java.util.List<Map<String, AttributeValue>> getAllItemsFromDb(String tableName) {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
            ScanResult result = dynamoDbClient.scan(scanRequest);
            return result.getItems();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Create a test user with specified attributes
     */
    protected void createTestUser(String id, String name, String email, String age) {
        Map<String, AttributeValue> user = new HashMap<>();
        user.put("id", new AttributeValue(id));
        user.put("name", new AttributeValue(name));
        if (email != null) user.put("email", new AttributeValue(email));
        if (age != null) user.put("age", new AttributeValue().withN(age));
        putItemInDb("users", user);
    }

    /**
     * Create a test product with specified attributes
     */
    protected void createTestProduct(String productId, String category, String name, String price) {
        Map<String, AttributeValue> product = new HashMap<>();
        product.put("productId", new AttributeValue(productId));
        product.put("category", new AttributeValue(category));
        product.put("name", new AttributeValue(name));
        if (price != null) product.put("price", new AttributeValue().withN(price));
        putItemInDb("products", product);
    }

    /**
     * Verify that an item exists in the database with expected values
     */
    protected void verifyItemInDb(String tableName, Map<String, AttributeValue> key, 
                                String fieldName, String expectedValue) {
        Map<String, AttributeValue> item = getItemFromDb(tableName, key);
        assertThat(item).isNotNull();
        assertThat(item.get(fieldName)).isNotNull();
        assertThat(item.get(fieldName).getS()).isEqualTo(expectedValue);
    }

    /**
     * Verify that an item does not exist in the database
     */
    protected void verifyItemNotInDb(String tableName, Map<String, AttributeValue> key) {
        Map<String, AttributeValue> item = getItemFromDb(tableName, key);
        assertThat(item).isNull();
    }

    /**
     * Tests the DynamoDB connection with retry logic
     */
    private void testDynamoDbConnection() throws Exception {
        System.out.println("Testing DynamoDB connection...");
        int maxRetries = 15; // Increased from 10
        long retryDelay = 2000; // Increased from 1000ms
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Try to list tables - this is a simple operation that tests connectivity
                dynamoDbClient.listTables();
                System.out.println("DynamoDB connection successful on attempt " + attempt);
                return;
            } catch (Exception e) {
                System.out.println("Connection attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to connect to DynamoDB after " + maxRetries + " attempts", e);
                }
                Thread.sleep(retryDelay);
                // Don't increase delay too much to avoid very long waits
                if (retryDelay < 5000) {
                    retryDelay = Math.min(retryDelay * 2, 5000);
                }
            }
        }
    }
}
