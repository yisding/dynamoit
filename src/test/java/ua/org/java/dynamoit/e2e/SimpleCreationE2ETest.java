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
 * Simple E2E test for basic item creation functionality.
 * This test verifies that users can create new items in DynamoDB tables through the UI.
 */
class SimpleCreationE2ETest extends DynamoItE2ETestBase {

    @Test
    void shouldCreateNewUserItem() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Wait for application to load
        waitForUi(3000);
        
        // Create and select local profile
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi(2000);
        
        // Click the "Add Item" button (look for plus icon or add button)
        try {
            // Try to find add button by tooltip or icon
            robot.clickOn(robot.lookup((Button button) -> 
                button.getTooltip() != null && 
                button.getTooltip().getText().toLowerCase().contains("add")).queryButton());
        } catch (Exception e) {
            // Fallback: try to find by text
            try {
                robot.clickOn("Add");
            } catch (Exception e2) {
                // Fallback: use keyboard shortcut
                robot.press(KeyCode.CONTROL, KeyCode.N);
            }
        }
        waitForUi(1000);
        
        // Fill in the new user data
        // Assuming the form has fields for id, name, email, age
        robot.write("test-user-" + System.currentTimeMillis()); // id field
        robot.press(KeyCode.TAB);
        
        robot.write("Test User"); // name field
        robot.press(KeyCode.TAB);
        
        robot.write("testuser@example.com"); // email field
        robot.press(KeyCode.TAB);
        
        robot.write("28"); // age field
        
        // Save the item
        robot.press(KeyCode.ENTER); // or look for Save button
        waitForUi(2000);
        
        // Verify the item appears in the table
        assertThat(robot.lookup("Test User").tryQuery()).isPresent();
        assertThat(robot.lookup("testuser@example.com").tryQuery()).isPresent();
    }

    @Test
    void shouldCreateNewProductItem() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Wait for application to load
        waitForUi(3000);
        
        // Create and select local profile
        setupLocalProfile(robot);
        
        // Open products table
        robot.doubleClickOn("products");
        waitForUi(2000);
        
        // Click the "Add Item" button
        try {
            robot.clickOn(robot.lookup((Button button) -> 
                button.getTooltip() != null && 
                button.getTooltip().getText().toLowerCase().contains("add")).queryButton());
        } catch (Exception e) {
            try {
                robot.clickOn("Add");
            } catch (Exception e2) {
                robot.press(KeyCode.CONTROL, KeyCode.N);
            }
        }
        waitForUi(1000);
        
        // Fill in the new product data
        String testProductId = "PROD-TEST-" + System.currentTimeMillis();
        robot.write(testProductId); // productId field
        robot.press(KeyCode.TAB);
        
        robot.write("TestCategory"); // category field
        robot.press(KeyCode.TAB);
        
        robot.write("Test Product"); // name field
        robot.press(KeyCode.TAB);
        
        robot.write("19.99"); // price field
        
        // Save the item
        robot.press(KeyCode.ENTER);
        waitForUi(2000);
        
        // Verify the item appears in the table
        assertThat(robot.lookup("Test Product").tryQuery()).isPresent();
        assertThat(robot.lookup("TestCategory").tryQuery()).isPresent();
        
        // Verify the item was saved to the database
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("productId", new AttributeValue(testProductId));
        key.put("category", new AttributeValue("TestCategory"));
        
        Map<String, AttributeValue> item = getItemFromDb("products", key);
        assertThat(item).isNotNull();
        assertThat(item.get("name").getS()).isEqualTo("Test Product");
        assertThat(item.get("price").getN()).isEqualTo("19.99");
    }

    /**
     * Helper method to set up a local profile for testing
     */
    private void setupLocalProfile(FxRobot robot) throws InterruptedException {
        try {
            // Try to click add profile button
            robot.clickOn(robot.lookup((Button button) -> 
                button.getTooltip() != null && 
                button.getTooltip().getText().contains("Add a new profile")).queryButton());
        } catch (Exception e) {
            // Fallback: try to find any button that might be the add profile button
            try {
                robot.clickOn("Button");
            } catch (Exception e2) {
                // If all else fails, use keyboard shortcut
                robot.press(KeyCode.CONTROL, KeyCode.SHIFT, KeyCode.N);
            }
        }
        waitForUi(1000);
        
        // Click the "Local" tab if dialog opened
        try {
            robot.clickOn("Local");
            waitForUi(500);
        } catch (Exception e) {
            // Dialog might not have opened or already be on Local tab
        }
        
        // Fill in the local profile dialog
        robot.write(testProfileName);  // Profile name
        robot.press(KeyCode.TAB);
        robot.write(dynamoDbEndpoint);  // Endpoint URL
        
        // Click OK or press Enter
        try {
            robot.clickOn("OK");
        } catch (Exception e) {
            robot.press(KeyCode.ENTER);
        }
        waitForUi(1000);
        
        // Select the local profile
        try {
            robot.clickOn(testProfileName);
        } catch (Exception e) {
            // Profile might already be selected
        }
        waitForUi(2000);
    }
}
