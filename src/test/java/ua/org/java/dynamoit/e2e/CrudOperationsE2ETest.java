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
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive End-to-End tests for CRUD operations (Create, Read, Update, Delete).
 * Tests the complete data lifecycle in DynamoIt from UI interactions to database persistence.
 */
class CrudOperationsE2ETest extends DynamoItE2ETestBase {

    // ===========================================
    // CREATE (INSERT) OPERATIONS
    // ===========================================

    @Test
    void shouldCreateNewUserWithAllFields() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Click "Add Item" or "New Item" button
        robot.clickOn("Add Item");
        waitForUi();
        
        // Fill in complete user data
        robot.write("new-user-001");  // id
        robot.press(KeyCode.TAB);
        robot.write("David Wilson");  // name
        robot.press(KeyCode.TAB);
        robot.write("david.wilson@example.com");  // email
        robot.press(KeyCode.TAB);
        robot.write("42");  // age
        
        // Save the new item
        robot.clickOn("Save");
        waitForUi();
        
        // Verify item appears in UI
        assertThat(robot.lookup("David Wilson").tryQuery()).isPresent();
        
        // Verify item is persisted in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("new-user-001"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("David Wilson");
        assertThat(item.get("email").getS()).isEqualTo("david.wilson@example.com");
        assertThat(item.get("age").getN()).isEqualTo("42");
    }

    @Test
    void shouldCreateNewUserWithMinimalFields() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Only fill required fields
        robot.write("minimal-user");  // id (required)
        robot.press(KeyCode.TAB);
        robot.write("Jane Doe");  // name
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("minimal-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Jane Doe");
        // Optional fields should not be present or be null
        assertThat(item.get("email")).isNull();
    }

    @Test
    void shouldCreateNewProductWithCompositeKey() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Fill composite key (productId + category)
        robot.write("PROD-999");  // productId
        robot.press(KeyCode.TAB);
        robot.write("Software");  // category
        robot.press(KeyCode.TAB);
        robot.write("Test Automation Tool");  // name
        robot.press(KeyCode.TAB);
        robot.write("299.99");  // price
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("productId", new AttributeValue("PROD-999"));
        key.put("category", new AttributeValue("Software"));
        Map<String, AttributeValue> item = getItemFromDb("products", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Test Automation Tool");
        assertThat(item.get("price").getN()).isEqualTo("299.99");
    }

    @Test
    void shouldHandleCreateWithDuplicateKey() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Try to create with existing ID
        robot.write("1");  // This ID already exists (Alice)
        robot.press(KeyCode.TAB);
        robot.write("Duplicate User");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Should show error dialog or warning
        // The exact behavior depends on DynamoIt implementation
        // For now, verify the original user is still there
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("1"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Alice");  // Original should remain
    }

    // ===========================================
    // UPDATE OPERATIONS
    // ===========================================

    @Test
    void shouldUpdateSingleFieldInUser() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Find and edit Bob's age
        robot.rightClickOn("Bob");
        robot.clickOn("Edit");
        waitForUi();
        
        // Navigate to age field and update it
        robot.press(KeyCode.TAB, KeyCode.TAB, KeyCode.TAB);  // Navigate to age field
        robot.press(KeyCode.CONTROL, KeyCode.A);  // Select all
        robot.write("26");  // Change age from 25 to 26
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("2"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Bob");  // Name unchanged
        assertThat(item.get("age").getN()).isEqualTo("26");    // Age updated
    }

    @Test
    void shouldUpdateMultipleFieldsInUser() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.rightClickOn("Charlie");
        robot.clickOn("Edit");
        waitForUi();
        
        // Update name
        robot.press(KeyCode.TAB);  // Navigate to name field
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("Charles Updated");
        
        // Update email
        robot.press(KeyCode.TAB);
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("charles.updated@example.com");
        
        // Update age
        robot.press(KeyCode.TAB);
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("36");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify all changes in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("3"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Charles Updated");
        assertThat(item.get("email").getS()).isEqualTo("charles.updated@example.com");
        assertThat(item.get("age").getN()).isEqualTo("36");
    }

    @Test
    void shouldUpdateProductWithCompositeKey() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.rightClickOn("Laptop");
        robot.clickOn("Edit");
        waitForUi();
        
        // Update product name and price (cannot update key fields)
        robot.press(KeyCode.TAB, KeyCode.TAB);  // Navigate to name field
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("Gaming Laptop");
        
        robot.press(KeyCode.TAB);  // Navigate to price field
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("1299.99");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("productId", new AttributeValue("PROD-001"));
        key.put("category", new AttributeValue("Electronics"));
        Map<String, AttributeValue> item = getItemFromDb("products", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Gaming Laptop");
        assertThat(item.get("price").getN()).isEqualTo("1299.99");
    }

    // ===========================================
    // DELETE OPERATIONS
    // ===========================================

    @Test
    void shouldDeleteSingleUser() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Delete Bob
        robot.rightClickOn("Bob");
        robot.clickOn("Delete");
        waitForUi();
        
        // Confirm deletion
        robot.clickOn("Yes");  // or "Confirm" depending on dialog
        waitForUi();
        
        // Verify Bob is gone from UI
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
        
        // Verify deletion in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("2"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNull();
        
        // Verify other users are still there
        key.put("id", new AttributeValue("1"));
        item = getItemFromDb("users", key);
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Alice");
    }

    @Test
    void shouldDeleteMultipleUsers() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Select multiple users (if multi-select is supported)
        robot.clickOn("Alice");
        robot.press(KeyCode.CONTROL);
        robot.clickOn("Charlie");
        robot.release(KeyCode.CONTROL);
        
        robot.press(KeyCode.DELETE);  // Or right-click and delete
        waitForUi();
        
        robot.clickOn("Yes");
        waitForUi();
        
        // Verify both users are deleted
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("1"));
        assertThat(getItemFromDb("users", key)).isNull();
        
        key.put("id", new AttributeValue("3"));
        assertThat(getItemFromDb("users", key)).isNull();
        
        // Verify Bob is still there
        key.put("id", new AttributeValue("2"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Bob");
    }

    @Test
    void shouldDeleteProductWithCompositeKey() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.rightClickOn("Java Programming");
        robot.clickOn("Delete");
        waitForUi();
        
        robot.clickOn("Yes");
        waitForUi();
        
        // Verify deletion in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("productId", new AttributeValue("PROD-002"));
        key.put("category", new AttributeValue("Books"));
        Map<String, AttributeValue> item = getItemFromDb("products", key);
        
        assertThat(item).isNull();
        
        // Verify other product is still there
        key.put("productId", new AttributeValue("PROD-001"));
        key.put("category", new AttributeValue("Electronics"));
        item = getItemFromDb("products", key);
        assertThat(item).isNotNull();
    }

    @Test
    void shouldCancelDelete() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.rightClickOn("Alice");
        robot.clickOn("Delete");
        waitForUi();
        
        // Cancel the deletion
        robot.clickOn("No");  // or "Cancel"
        waitForUi();
        
        // Verify Alice is still there
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("1"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Alice");
    }

    // ===========================================
    // BULK OPERATIONS
    // ===========================================

    @Test
    void shouldCreateMultipleUsersInSequence() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Create first user
        robot.clickOn("Add Item");
        waitForUi();
        robot.write("bulk-1");
        robot.press(KeyCode.TAB);
        robot.write("Bulk User 1");
        robot.clickOn("Save");
        waitForUi();
        
        // Create second user
        robot.clickOn("Add Item");
        waitForUi();
        robot.write("bulk-2");
        robot.press(KeyCode.TAB);
        robot.write("Bulk User 2");
        robot.clickOn("Save");
        waitForUi();
        
        // Create third user
        robot.clickOn("Add Item");
        waitForUi();
        robot.write("bulk-3");
        robot.press(KeyCode.TAB);
        robot.write("Bulk User 3");
        robot.clickOn("Save");
        waitForUi();
        
        // Verify all three users in database
        for (int i = 1; i <= 3; i++) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue("bulk-" + i));
            Map<String, AttributeValue> item = getItemFromDb("users", key);
            
            assertThat(item).isNotNull();
            assertThat(item.get("name").getS()).isEqualTo("Bulk User " + i);
        }
    }

    // ===========================================
    // VALIDATION AND ERROR HANDLING
    // ===========================================

    @Test
    void shouldValidateRequiredFields() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Try to save without required ID field
        robot.write("");  // Empty ID
        robot.press(KeyCode.TAB);
        robot.write("No ID User");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Should show validation error or prevent save
        // The exact behavior depends on DynamoIt implementation
        // At minimum, verify the item was not created
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(""));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNull();
    }

    @Test
    void shouldHandleSpecialCharactersInData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Add Item");
        waitForUi();
        
        // Use special characters and unicode
        robot.write("special-char-user");
        robot.press(KeyCode.TAB);
        robot.write("José García-López & Co. 测试 🎉");
        robot.press(KeyCode.TAB);
        robot.write("jose+garcia@test.co.uk");
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify special characters are handled correctly
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("special-char-user"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("José García-López & Co. 测试 🎉");
        assertThat(item.get("email").getS()).isEqualTo("jose+garcia@test.co.uk");
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private void setupLocalProfile(FxRobot robot) throws InterruptedException {
        waitForUi();
        
        // Debug: Print all visible nodes to understand what's available
        System.out.println("=== DEBUG: Looking for existing test-local profile ===");
        try {
            robot.lookup("test-local").queryButton();
            System.out.println("Found existing test-local profile!");
        } catch (Exception e) {
            System.out.println("test-local profile not found, need to create it");
            
            // Try to create the profile
            System.out.println("=== DEBUG: Attempting to create profile ===");
            
            // Try multiple strategies to click the "Add" button
            boolean clicked = false;
            
            // Strategy 1: Look for button with tooltip
            try {
                robot.clickOn(robot.lookup((Button button) -> button.getTooltip() != null && 
                    button.getTooltip().getText().contains("Add a new profile")).queryButton());
                clicked = true;
                System.out.println("Strategy 1 (tooltip) worked!");
            } catch (Exception ex) {
                System.out.println("Strategy 1 (tooltip) failed: " + ex.getMessage());
            }
            
            // Strategy 2: Look for the first button in a toolbar
            if (!clicked) {
                try {
                    robot.clickOn(".tool-bar .button");
                    clicked = true;
                    System.out.println("Strategy 2 (toolbar button) worked!");
                } catch (Exception ex) {
                    System.out.println("Strategy 2 (toolbar button) failed: " + ex.getMessage());
                }
            }
            
            // Strategy 3: Look for any button (should be the add button since it's the first one)
            if (!clicked) {
                try {
                    robot.clickOn("Button");
                    clicked = true;
                    System.out.println("Strategy 3 (any button) worked!");
                } catch (Exception ex) {
                    System.out.println("Strategy 3 (any button) failed: " + ex.getMessage());
                    throw new RuntimeException("Could not find the Add Profile button");
                }
            }
            
            waitForUi();
            System.out.println("=== DEBUG: Attempting to click Local tab ===");
            
            // The dialog should now be open, click the "Local" tab
            try {
                robot.clickOn("Local");
                System.out.println("Clicked Local tab successfully");
            } catch (Exception ex) {
                System.out.println("Failed to click Local tab: " + ex.getMessage());
                throw ex;
            }
            waitForUi();
            
            System.out.println("=== DEBUG: Filling in profile details ===");
            
            // Try to click on and fill the profile name field more explicitly
            try {
                // Look for text fields in the dialog
                var textFields = robot.lookup(".text-field").queryAll();
                System.out.println("Found " + textFields.size() + " text fields in dialog");
                
                // Click on the first text field (should be profile name)
                robot.clickOn(".text-field");
                robot.write("test-local");
                System.out.println("Filled profile name field");
                
                robot.press(KeyCode.TAB);
                
                // Click on the second text field (should be endpoint URL)
                robot.write(dynamoDbEndpoint);
                System.out.println("Filled endpoint URL field: " + dynamoDbEndpoint);
                
            } catch (Exception ex) {
                System.out.println("Error filling form fields: " + ex.getMessage());
                // Fallback to the old method
                robot.write("test-local");
                robot.press(KeyCode.TAB);
                robot.write(dynamoDbEndpoint);
            }
            
            System.out.println("=== DEBUG: Attempting to save profile ===");
            
            // Check if OK button is enabled before clicking
            try {
                var okButton = robot.lookup("OK").queryButton();
                System.out.println("OK button found, disabled: " + okButton.isDisabled());
            } catch (Exception ex) {
                System.out.println("Could not find OK button: " + ex.getMessage());
            }
            
            // Click OK to save
            try {
                robot.clickOn("OK");
                System.out.println("Clicked OK successfully");
            } catch (Exception ex) {
                System.out.println("Failed to click OK: " + ex.getMessage());
                // Try alternative selectors for the OK button
                try {
                    robot.clickOn(".button:containing('OK')");
                    System.out.println("Clicked OK with alternative selector");
                } catch (Exception ex2) {
                    System.out.println("Failed to click OK with alternative selector: " + ex2.getMessage());
                    throw ex;
                }
            }
            waitForUi();
            
            // Wait a bit more for the profile to be created and the UI to update
            System.out.println("=== DEBUG: Waiting for profile to appear in UI ===");
            Thread.sleep(5000);  // Increased wait time
            
            // Debug: Try to find any toggle buttons that might be our profile
            try {
                var toggleButtons = robot.lookup(".toggle-button").queryAllAs(ToggleButton.class);
                System.out.println("Found " + toggleButtons.size() + " toggle buttons:");
                for (ToggleButton tb : toggleButtons) {
                    System.out.println("  - ToggleButton text: '" + tb.getText() + "'");
                }
            } catch (Exception ex) {
                System.out.println("Could not query toggle buttons: " + ex.getMessage());
            }
            
            System.out.println("=== DEBUG: Profile creation completed ===");
        }
        
        System.out.println("=== DEBUG: Attempting to select test-local profile ===");
        
        // Select the local profile - try different approaches
        try {
            robot.clickOn("test-local");
            System.out.println("Selected test-local profile successfully");
        } catch (Exception ex) {
            System.out.println("Failed to select test-local profile with text lookup: " + ex.getMessage());
            
            // Try selecting by ToggleButton with the specific text
            try {
                robot.clickOn((ToggleButton toggleButton) -> "test-local".equals(toggleButton.getText()));
                System.out.println("Selected test-local profile using ToggleButton predicate successfully");
            } catch (Exception ex2) {
                System.out.println("Failed to select test-local profile with ToggleButton predicate: " + ex2.getMessage());
                throw ex2;
            }
        }
        waitForUi();
    }
}
