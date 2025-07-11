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
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End tests for the main DynamoIt application functionality.
 * These tests exercise the complete application stack from UI to database.
 */
class DynamoItMainE2ETest extends DynamoItE2ETestBase {

    @Test
    void shouldDisplayTablesInLocalProfile() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Wait for application to load
        waitForUi();
        
        // Create a local profile pointing to our test DynamoDB
        robot.clickOn("Profiles").clickOn("Add Local Profile");
        waitForUi();
        
        // Fill in the local profile dialog
        robot.write("test-local");  // Profile name
        robot.press(KeyCode.TAB);
        robot.write(dynamoDbEndpoint);  // Endpoint URL
        robot.clickOn("Save");
        waitForUi();
        
        // Select the local profile
        robot.clickOn("test-local");
        waitForUi();
        
        // Verify tables are displayed
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        assertThat(robot.lookup("products").tryQuery()).isPresent();
    }

    @Test
    void shouldOpenTableAndDisplayItems() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Set up local profile (simplified - assumes profile exists from previous test)
        setupLocalProfile(robot);
        
        // Double-click on users table to open it
        robot.doubleClickOn("users");
        waitForUi();
        
        // Verify table tab is opened
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        
        // Verify user data is displayed
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldEditItemAndPersistChanges() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Find and edit Alice's record
        robot.doubleClickOn("Alice");
        waitForUi();
        
        // Change name to Alice-Updated
        robot.press(KeyCode.CONTROL, KeyCode.A);  // Select all
        robot.write("Alice-Updated");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify the change is reflected in the UI
        assertThat(robot.lookup("Alice-Updated").tryQuery()).isPresent();
        
        // Verify the change is persisted in the database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("1"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Alice-Updated");
    }

    @Test
    void shouldFilterTableData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply a filter
        robot.clickOn("Filter");
        waitForUi();
        
        // Filter by name contains "Bob"
        robot.write("name contains Bob");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify only Bob is shown
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Alice").tryQuery()).isEmpty();
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();
    }

    @Test
    void shouldCreateNewItem() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Click "Add Item" button
        robot.clickOn("Add Item");
        waitForUi();
        
        // Fill in new item details
        robot.write("4");  // id
        robot.press(KeyCode.TAB);
        robot.write("Diana");  // name
        robot.press(KeyCode.TAB);
        robot.write("diana@example.com");  // email
        robot.press(KeyCode.TAB);
        robot.write("28");  // age
        
        robot.clickOn("Save");
        waitForUi();
        
        // Verify new item appears in UI
        assertThat(robot.lookup("Diana").tryQuery()).isPresent();
        
        // Verify new item is persisted in database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("4"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Diana");
        assertThat(item.get("email").getS()).isEqualTo("diana@example.com");
        assertThat(item.get("age").getN()).isEqualTo("28");
    }

    @Test
    void shouldDeleteItem() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Right-click on Charlie to open context menu
        robot.rightClickOn("Charlie");
        waitForUi();
        
        // Click delete
        robot.clickOn("Delete");
        waitForUi();
        
        // Confirm deletion
        robot.clickOn("Yes");
        waitForUi();
        
        // Verify Charlie is no longer in UI
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();
        
        // Verify Charlie is deleted from database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue("3"));
        Map<String, AttributeValue> item = getItemFromDb("users", key);
        
        assertThat(item).isNull();
    }

    /**
     * Helper method to set up a local profile for testing
     */
    private void setupLocalProfile(FxRobot robot) throws InterruptedException {
        // Check if profile already exists, if not create it
        if (robot.lookup("test-local").tryQuery().isEmpty()) {
            robot.clickOn("Profiles").clickOn("Add Local Profile");
            waitForUi();
            
            robot.write("test-local");
            robot.press(KeyCode.TAB);
            robot.write(dynamoDbEndpoint);
            robot.clickOn("Save");
            waitForUi();
        }
        
        // Select the local profile
        robot.clickOn("test-local");
        waitForUi();
    }
}
