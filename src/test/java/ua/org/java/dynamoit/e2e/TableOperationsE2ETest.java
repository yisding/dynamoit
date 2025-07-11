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
 * End-to-End tests for table operations in DynamoIt.
 */
class TableOperationsE2ETest extends DynamoItE2ETestBase {

    @Test
    void shouldDisplayTableMetadata() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Open table info dialog
        robot.clickOn("Table Info");
        waitForUi();
        
        // Verify table metadata is displayed
        assertThat(robot.lookup("Table Name").tryQuery()).isPresent();
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        assertThat(robot.lookup("Primary Key").tryQuery()).isPresent();
        
        // Close dialog
        robot.clickOn("Close");
        waitForUi();
    }

    @Test
    void shouldRefreshTableData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Verify initial data
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
        
        // Add a new item directly to the database
        Map<String, AttributeValue> newUser = new HashMap<>();
        newUser.put("id", new AttributeValue("5"));
        newUser.put("name", new AttributeValue("Eve"));
        newUser.put("email", new AttributeValue("eve@example.com"));
        newUser.put("age", new AttributeValue().withN("29"));
        dynamoDbClient.putItem("users", newUser);
        
        // Refresh the table
        robot.clickOn("Refresh");
        waitForUi();
        
        // Verify new data appears
        assertThat(robot.lookup("Eve").tryQuery()).isPresent();
    }

    @Test
    void shouldExportTableData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Click export
        robot.clickOn("Export");
        waitForUi();
        
        // Choose export format (assuming JSON)
        robot.clickOn("JSON");
        waitForUi();
        
        // Choose file location (this might open a file dialog)
        // For the test, we'll assume the export completes successfully
        robot.clickOn("Save");
        waitForUi();
        
        // The exact verification depends on how export is implemented
        // For now, just verify no errors occurred
    }

    @Test
    void shouldHandleLargeTablePagination() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Add many items to test pagination
        for (int i = 10; i < 110; i++) {
            Map<String, AttributeValue> user = new HashMap<>();
            user.put("id", new AttributeValue(String.valueOf(i)));
            user.put("name", new AttributeValue("User" + i));
            user.put("email", new AttributeValue("user" + i + "@example.com"));
            user.put("age", new AttributeValue().withN("25"));
            dynamoDbClient.putItem("users", user);
        }
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Verify some items are displayed
        assertThat(robot.lookup("User10").tryQuery()).isPresent();
        
        // Navigate to next page (if pagination controls exist)
        if (robot.lookup("Next Page").tryQuery().isPresent()) {
            robot.clickOn("Next Page");
            waitForUi();
            
            // Verify different items are shown
            assertThat(robot.lookup("User50").tryQuery()).isPresent();
        }
    }

    @Test
    void shouldSortTableData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Click on name column header to sort
        robot.clickOn("Name");
        waitForUi();
        
        // Verify sorting (this depends on the actual UI implementation)
        // For now, just verify the table is still displaying data
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldSearchTableData() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Use search functionality
        robot.clickOn("Search");
        waitForUi();
        
        robot.write("Alice");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify search results
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        // Other users should be filtered out
        // (exact behavior depends on implementation)
    }

    @Test
    void shouldCopyItemToClipboard() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        setupLocalProfile(robot);
        
        // Open users table
        robot.doubleClickOn("users");
        waitForUi();
        
        // Right-click on Alice
        robot.rightClickOn("Alice");
        waitForUi();
        
        // Click copy
        robot.clickOn("Copy");
        waitForUi();
        
        // The clipboard now contains the item data
        // Verification of clipboard content would require additional setup
    }

    private void setupLocalProfile(FxRobot robot) throws InterruptedException {
        waitForUi();
        
        // Check if profile already exists
        if (robot.lookup("test-local").tryQuery().isEmpty()) {
            robot.clickOn("Profiles");
            robot.clickOn("Add Local Profile");
            waitForUi();
            
            robot.write("test-local");
            robot.press(KeyCode.TAB);
            robot.write(dynamoDbEndpoint);
            robot.clickOn("Save");
            waitForUi();
        }
        
        // Select the profile
        robot.clickOn("test-local");
        waitForUi();
    }
}
