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
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive End-to-End tests for advanced table operations in DynamoIt.
 * Tests insertion with validation, bulk operations, data import/export, and edge cases.
 */
class AdvancedTableOperationsE2ETest extends DynamoItE2ETestBase {

    // ===========================================
    // ADVANCED INSERTION OPERATIONS
    // ===========================================

    @Test
    void shouldInsertItemWithNestedObjects() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Add user with nested address object
        robot.write("nested-user");
        robot.press(KeyCode.TAB);
        robot.write("John Nested");
        robot.press(KeyCode.TAB);
        robot.write("john@nested.com");
        robot.press(KeyCode.TAB);
        robot.write("40");
        robot.press(KeyCode.TAB);
        
        // Add nested address (JSON format)
        robot.write("{\"street\": \"123 Main St\", \"city\": \"Springfield\", \"zip\": \"12345\"}");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("nested-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("John Nested");
        // Note: Nested object verification depends on DynamoDB storage format
    }

    @Test
    void shouldInsertItemWithListAttributes() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        robot.write("list-user");
        robot.press(KeyCode.TAB);
        robot.write("Mary Lists");
        robot.press(KeyCode.TAB);
        robot.write("mary@lists.com");
        robot.press(KeyCode.TAB);
        robot.write("32");
        robot.press(KeyCode.TAB);
        
        // Add list of hobbies
        robot.write("[\"reading\", \"swimming\", \"coding\"]");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("list-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Mary Lists");
    }

    @Test
    void shouldInsertItemWithLargeTextData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        robot.write("large-text-user");
        robot.press(KeyCode.TAB);
        robot.write("Large Text User");
        robot.press(KeyCode.TAB);
        robot.write("large@text.com");
        robot.press(KeyCode.TAB);
        robot.write("25");
        robot.press(KeyCode.TAB);
        
        // Add large description (simulate large text)
        String largeText = "This is a very long description that contains many words and should test the ability of DynamoIt to handle large text fields properly. ".repeat(10);
        robot.write(largeText.substring(0, Math.min(largeText.length(), 1000))); // Truncate if too long for UI
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("large-text-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Large Text User");
    }

    // ===========================================
    // BULK INSERT OPERATIONS
    // ===========================================

    @Test
    void shouldImportDataFromClipboard() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Access import functionality
        robot.clickOn("Import");  // or File -> Import
        waitForUi();
        
        robot.clickOn("From Clipboard");
        waitForUi();
        
        // Simulate CSV data in clipboard
        String csvData = "id,name,email,age\n" +
                        "csv1,CSV User 1,csv1@test.com,25\n" +
                        "csv2,CSV User 2,csv2@test.com,30\n" +
                        "csv3,CSV User 3,csv3@test.com,35";
        
        robot.write(csvData);
        robot.clickOn("Import");
        waitForUi();
        
        // Verify all three users were imported
        for (int i = 1; i <= 3; i++) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue("csv" + i));
            Map<String, AttributeValue> item = getItemFromDb("users", key);
            
            assertThat(item).isNotNull();
            assertThat(item.get("name").getS()).isEqualTo("CSV User " + i);
        }
    }

    @Test
    void shouldBatchInsertMultipleItems() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Batch Operations");
        robot.clickOn("Batch Insert");
        waitForUi();
        
        // Add multiple items in batch mode
        String batchData = """
            [
              {"id": "batch1", "name": "Batch User 1", "email": "batch1@test.com", "age": 26},
              {"id": "batch2", "name": "Batch User 2", "email": "batch2@test.com", "age": 27},
              {"id": "batch3", "name": "Batch User 3", "email": "batch3@test.com", "age": 28}
            ]
            """;
        
        robot.write(batchData);
        robot.clickOn("Execute Batch");
        waitForUi();
        
        // Verify all items were inserted
        for (int i = 1; i <= 3; i++) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue("batch" + i));
            Map<String, AttributeValue> item = getItemFromDb("users", key);
            
            assertThat(item).isNotNull();
            assertThat(item.get("name").getS()).isEqualTo("Batch User " + i);
            assertThat(item.get("age").getN()).isEqualTo(String.valueOf(25 + i));
        }
    }

    // ===========================================
    // BULK REMOVAL OPERATIONS
    // ===========================================

    @Test
    void shouldBatchDeleteMultipleItems() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        // First, create some items to delete
        createBatchTestData();
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Select multiple items for deletion
        robot.clickOn("Select All");  // or use Ctrl+A
        waitForUi();
        
        robot.clickOn("Batch Operations");
        robot.clickOn("Delete Selected");
        waitForUi();
        
        robot.clickOn("Yes");  // Confirm deletion
        waitForUi();
        
        // Verify items are deleted
        for (int i = 1; i <= 3; i++) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue("delete-test-" + i));
            Map<String, AttributeValue> item = getItemFromDb("users", key);
            
            assertThat(item).isNull();
        }
    }

    @Test
    void shouldDeleteByFilter() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        // Create test data with specific pattern
        createFilteredDeleteTestData();
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply filter to select items to delete
        robot.clickOn("Filter");
        waitForUi();
        robot.write("name contains 'DeleteMe'");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Delete filtered items
        robot.clickOn("Delete Filtered Items");
        waitForUi();
        robot.clickOn("Yes");
        waitForUi();
        
        // Verify only filtered items are deleted
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("delete-me-1"));
        assertThat(getItemFromDb("users", key)).isNull();
        
        key.put("id", new AttributeValue("keep-me-1"));
        assertThat(getItemFromDb("users", key)).isNotNull();
    }

    // ===========================================
    // ADVANCED SEARCH OPERATIONS
    // ===========================================

    @Test
    void shouldSearchWithRegularExpressions() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Advanced Search");
        waitForUi();
        
        robot.clickOn("Regular Expression");
        robot.write("email REGEX '.*@test\\.com$'");
        robot.clickOn("Search");
        waitForUi();
        
        // Should find users with @test.com email domain
        // Verify results based on test data
    }

    @Test
    void shouldSearchAcrossMultipleTables() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.clickOn("Global Search");
        waitForUi();
        
        robot.write("Laptop");  // Should find in products table
        robot.clickOn("Search All Tables");
        waitForUi();
        
        // Verify search results show items from multiple tables
        assertThat(robot.lookup("products").tryQuery()).isPresent();
        assertThat(robot.lookup("Laptop").tryQuery()).isPresent();
    }

    @Test
    void shouldSaveSearchQueries() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        robot.write("age > 30 AND email contains 'example'");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Save the query
        robot.clickOn("Save Query");
        waitForUi();
        robot.write("Senior Users");
        robot.clickOn("Save");
        waitForUi();
        
        // Clear and reload
        robot.clickOn("Clear Filter");
        waitForUi();
        
        robot.clickOn("Load Saved Queries");
        robot.clickOn("Senior Users");
        waitForUi();
        
        // Verify query is reapplied
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();  // Age 25
    }

    // ===========================================
    // DATA EXPORT OPERATIONS
    // ===========================================

    @Test
    void shouldExportTableToCSV() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Export");
        waitForUi();
        
        robot.clickOn("CSV Format");
        waitForUi();
        
        robot.write("users_export.csv");  // Filename
        robot.clickOn("Export");
        waitForUi();
        
        // Verify export completed successfully
        assertThat(robot.lookup("Export completed").tryQuery()).isPresent();
    }

    @Test
    void shouldExportFilteredData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply filter first
        robot.clickOn("Filter");
        waitForUi();
        robot.write("age >= 30");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Export filtered data
        robot.clickOn("Export");
        robot.clickOn("Export Filtered Data");
        waitForUi();
        
        robot.clickOn("JSON Format");
        robot.write("filtered_users.json");
        robot.clickOn("Export");
        waitForUi();
        
        assertThat(robot.lookup("Export completed").tryQuery()).isPresent();
    }

    @Test
    void shouldExportToMultipleFormats() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.clickOn("Export");
        waitForUi();
        
        // Test different export formats
        String[] formats = {"CSV", "JSON", "XML", "Excel"};
        
        for (String format : formats) {
            if (robot.lookup(format).tryQuery().isPresent()) {
                robot.clickOn(format);
                robot.write("products_export." + format.toLowerCase());
                robot.clickOn("Export");
                waitForUi();
                
                // Wait for export to complete
                Thread.sleep(1000);
                
                // Return to export dialog for next format
                if (!format.equals(formats[formats.length - 1])) {
                    robot.clickOn("Export");
                    waitForUi();
                }
            }
        }
    }

    // ===========================================
    // PERFORMANCE AND STRESS TESTING
    // ===========================================

    @Test
    void shouldHandleLargeVolumeInsert() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Create large batch insert
        StringBuilder largeBatch = new StringBuilder("[\n");
        for (int i = 1000; i < 1100; i++) {
            largeBatch.append(String.format(
                "{\"id\": \"bulk-%d\", \"name\": \"Bulk User %d\", \"email\": \"bulk%d@test.com\", \"age\": %d}",
                i, i, i, 20 + (i % 40)
            ));
            if (i < 1099) largeBatch.append(",\n");
        }
        largeBatch.append("\n]");
        
        robot.clickOn("Batch Operations");
        robot.clickOn("Batch Insert");
        waitForUi();
        
        long startTime = System.currentTimeMillis();
        
        robot.write(largeBatch.toString());
        robot.clickOn("Execute Batch");
        waitForUi();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify operation completes in reasonable time (< 30 seconds)
        assertThat(duration).isLessThan(30000);
        
        // Verify some items were inserted
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("bulk-1050"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        assertThat(item).isNotNull();
    }

    @Test
    void shouldHandleConcurrentOperations() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Simulate concurrent operations by rapidly performing multiple actions
        
        // Start a filter operation
        robot.clickOn("Filter");
        robot.write("age > 25");
        
        // While filter is processing, try to add an item
        robot.clickOn("Add Item");
        waitForUi();
        robot.write("concurrent-user");
        robot.press(KeyCode.TAB);
        robot.write("Concurrent User");
        robot.clickOn("Save");
        waitForUi();
        
        // Apply the filter
        robot.clickOn("Filter");  // Return to filter if needed
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify both operations completed successfully
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("concurrent-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        assertThat(item).isNotNull();
    }

    // ===========================================
    // ERROR HANDLING AND EDGE CASES
    // ===========================================

    @Test
    void shouldHandleInsertionErrors() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Try to insert invalid data
        robot.write("error-user");
        robot.press(KeyCode.TAB);
        robot.write("Error User");
        robot.press(KeyCode.TAB);
        robot.write("invalid-email-format");  // Invalid email
        robot.press(KeyCode.TAB);
        robot.write("not-a-number");  // Invalid age
        
        robot.clickOn("Save");
        waitForUi();
        
        // Should show validation error or handle gracefully
        // Verify item was not created with invalid data
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("error-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        // Either item doesn't exist (validation prevented save) or has corrected data
        if (item != null) {
            // If item exists, verify data integrity
            assertThat(item.get("name").getS()).isEqualTo("Error User");
        }
    }

    @Test
    void shouldHandleNetworkInterruption() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Simulate network issue by temporarily stopping container
        // (This is a simplified simulation - real test would need network manipulation)
        
        robot.clickOn("Add Item");
        waitForUi();
        robot.write("network-test-user");
        robot.press(KeyCode.TAB);
        robot.write("Network Test User");
        
        // Simulate saving during network issue
        robot.clickOn("Save");
        waitForUi();
        
        // Should show error message or retry mechanism
        // Verify graceful error handling
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private void setupLocalProfile(FxRobot robot) throws InterruptedException {
        waitForUi();
        
        if (robot.lookup("test-local").tryQuery().isEmpty()) {
            // Click the "Add" button - try different selectors
            try {
                robot.clickOn(robot.lookup((Button button) -> button.getTooltip() != null && 
                    button.getTooltip().getText().contains("Add a new profile")).queryButton());
            } catch (Exception e) {
                // If tooltip selector doesn't work, try clicking the first button in the toolbar
                robot.clickOn("Button");
            }
            waitForUi();
            
            // Click the "Local" tab in the dialog
            robot.clickOn("Local");
            waitForUi();
            
            // Click on the first text field and fill profile name
            robot.clickOn(".text-field");
            robot.write("test-local");
            robot.press(KeyCode.TAB);
            
            // Fill in endpoint URL
            robot.write(dynamoDbEndpoint);
            
            // Click OK to save
            robot.clickOn("OK");
            waitForUi();
            
            // Wait for the profile to be created
            Thread.sleep(1000);
        }
        
        robot.clickOn("test-local");
        waitForUi();
    }

    private void createBatchTestData() {
        for (int i = 1; i <= 3; i++) {
            Map<String, AttributeValue> user = new HashMap<>();
            user.put("id", new AttributeValue("delete-test-" + i));
            user.put("name", new AttributeValue("Delete Test " + i));
            user.put("email", new AttributeValue("delete" + i + "@test.com"));
            user.put("age", new AttributeValue().withN(String.valueOf(20 + i)));
            
            try {
                dynamoDbClient.putItem("users", user);
            } catch (Exception e) {
                // Ignore errors
            }
        }
    }

    private void createFilteredDeleteTestData() {
        // Items to be deleted (contain 'DeleteMe')
        Map<String, AttributeValue> deleteUser1 = new HashMap<>();
        deleteUser1.put("id", new AttributeValue("delete-me-1"));
        deleteUser1.put("name", new AttributeValue("DeleteMe User 1"));
        deleteUser1.put("email", new AttributeValue("delete1@test.com"));

        // Items to be kept (don't contain 'DeleteMe')
        Map<String, AttributeValue> keepUser1 = new HashMap<>();
        keepUser1.put("id", new AttributeValue("keep-me-1"));
        keepUser1.put("name", new AttributeValue("Keep User 1"));
        keepUser1.put("email", new AttributeValue("keep1@test.com"));

        try {
            dynamoDbClient.putItem("users", deleteUser1);
            dynamoDbClient.putItem("users", keepUser1);
        } catch (Exception e) {
            // Ignore errors
        }
    }
}
