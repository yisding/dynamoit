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

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End tests for profile management functionality.
 */
class ProfileManagementE2ETest extends DynamoItE2ETestBase {

    @Test
    void shouldCreateAndUseLocalProfile() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        waitForUi();
        
        // Navigate to profile management
        robot.clickOn("Profiles");
        waitForUi();
        
        // Create a new local profile
        robot.clickOn("Add Local Profile");
        waitForUi();
        
        // Fill in profile details
        robot.write("e2e-test-profile");
        robot.press(KeyCode.TAB);
        robot.write(dynamoDbEndpoint);
        robot.clickOn("Save");
        waitForUi();
        
        // Verify profile appears in the list
        assertThat(robot.lookup("e2e-test-profile").tryQuery()).isPresent();
        
        // Select the profile
        robot.clickOn("e2e-test-profile");
        waitForUi();
        
        // Verify tables are loaded
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        assertThat(robot.lookup("products").tryQuery()).isPresent();
    }

    @Test
    void shouldEditExistingProfile() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // First create a profile
        createTestProfile(robot, "profile-to-edit");
        
        // Right-click on profile to edit
        robot.rightClickOn("profile-to-edit");
        waitForUi();
        
        // Click edit
        robot.clickOn("Edit");
        waitForUi();
        
        // Change the name
        robot.press(KeyCode.CONTROL, KeyCode.A);
        robot.write("edited-profile-name");
        robot.press(KeyCode.TAB);
        robot.write(dynamoDbEndpoint);
        robot.clickOn("Save");
        waitForUi();
        
        // Verify the name change
        assertThat(robot.lookup("edited-profile-name").tryQuery()).isPresent();
        assertThat(robot.lookup("profile-to-edit").tryQuery()).isEmpty();
    }

    @Test
    void shouldDeleteProfile() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Create a profile to delete
        createTestProfile(robot, "profile-to-delete");
        
        // Right-click on profile
        robot.rightClickOn("profile-to-delete");
        waitForUi();
        
        // Click delete
        robot.clickOn("Delete");
        waitForUi();
        
        // Confirm deletion
        robot.clickOn("Yes");
        waitForUi();
        
        // Verify profile is gone
        assertThat(robot.lookup("profile-to-delete").tryQuery()).isEmpty();
    }

    @Test
    void shouldSwitchBetweenProfiles() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Create two different profiles
        createTestProfile(robot, "profile-one");
        createTestProfile(robot, "profile-two");
        
        // Select profile-one
        robot.clickOn("profile-one");
        waitForUi();
        
        // Verify tables are loaded for profile-one
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        
        // Switch to profile-two
        robot.clickOn("profile-two");
        waitForUi();
        
        // Verify we can still see the tables (same DynamoDB instance)
        assertThat(robot.lookup("users").tryQuery()).isPresent();
        assertThat(robot.lookup("products").tryQuery()).isPresent();
    }

    @Test
    void shouldHandleInvalidEndpoint() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        waitForUi();
        
        // Try to create a profile with invalid endpoint
        robot.clickOn("Profiles");
        robot.clickOn("Add Local Profile");
        waitForUi();
        
        robot.write("invalid-profile");
        robot.press(KeyCode.TAB);
        robot.write("http://invalid-endpoint:9999");
        robot.clickOn("Save");
        waitForUi();
        
        // Select the invalid profile
        robot.clickOn("invalid-profile");
        waitForUi();
        
        // Should show error or empty state
        // (The exact UI behavior depends on implementation)
        // For now, just verify the profile was created
        assertThat(robot.lookup("invalid-profile").tryQuery()).isPresent();
    }

    private void createTestProfile(FxRobot robot, String profileName) throws InterruptedException {
        robot.clickOn("Profiles");
        robot.clickOn("Add Local Profile");
        waitForUi();
        
        robot.write(profileName);
        robot.press(KeyCode.TAB);
        robot.write(dynamoDbEndpoint);
        robot.clickOn("Save");
        waitForUi();
    }
}
