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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive End-to-End tests for searching and filtering functionality in DynamoIt.
 * Tests various filter types, search operators, and complex query scenarios.
 */
class SearchAndFilterE2ETest extends DynamoItE2ETestBase {

    @BeforeEach
    void setupAdditionalTestData() {
        // Add more diverse test data for comprehensive filtering tests
        createAdditionalTestUsers();
        createAdditionalTestProducts();
    }

    // ===========================================
    // BASIC FILTERING OPERATIONS
    // ===========================================

    @Test
    void shouldFilterByExactMatch() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply exact match filter
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("name = Alice");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify only Alice is shown
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();
    }

    @Test
    void shouldFilterByContains() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("email contains example.com");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // All users should be visible (all have example.com emails)
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldFilterByBeginsWith() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("name begins_with A");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Only Alice and any other names starting with 'A'
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
    }

    @Test
    void shouldFilterByNotEquals() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("name <> Alice");  // or "name != Alice"
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Everyone except Alice should be visible
        assertThat(robot.lookup("Alice").tryQuery()).isEmpty();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldFilterByNotContains() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("email not contains bob");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Everyone except Bob should be visible
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    // ===========================================
    // NUMERIC FILTERING
    // ===========================================

    @Test
    void shouldFilterByNumericGreaterThan() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("age > 30");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Only users older than 30
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();  // age 35
        assertThat(robot.lookup("Alice").tryQuery()).isEmpty();      // age 30
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();        // age 25
    }

    @Test
    void shouldFilterByNumericLessThan() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("age < 30");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Only users younger than 30
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();      // age 25
        assertThat(robot.lookup("Alice").tryQuery()).isEmpty();      // age 30
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();    // age 35
    }

    @Test
    void shouldFilterByNumericBetween() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("age between 25 and 35");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Users aged 25-35 inclusive
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();    // age 30
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();      // age 25
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();  // age 35
    }

    // ===========================================
    // EXISTENCE FILTERING
    // ===========================================

    @Test
    void shouldFilterByAttributeExists() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("email exists");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // All users with email field should be visible
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldFilterByAttributeNotExists() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("phone not exists");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // All users without phone field should be visible
        // (assuming phone field doesn't exist in test data)
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    // ===========================================
    // COMPLEX FILTERING
    // ===========================================

    @Test
    void shouldFilterWithMultipleConditionsAnd() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("age > 25 AND email contains example");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Users older than 25 AND with example in email
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();    // age 30
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();  // age 35
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();        // age 25 (not > 25)
    }

    @Test
    void shouldFilterWithMultipleConditionsOr() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("name = Alice OR name = Charlie");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Either Alice OR Charlie
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
    }

    @Test
    void shouldFilterWithParentheses() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("(name = Alice OR name = Bob) AND age >= 25");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // (Alice OR Bob) AND age >= 25
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();    // Alice, age 30
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();      // Bob, age 25
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();    // Charlie doesn't match name condition
    }

    // ===========================================
    // PRODUCT TABLE FILTERING
    // ===========================================

    @Test
    void shouldFilterProductsByCategory() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("category = Electronics");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Only Electronics products
        assertThat(robot.lookup("Laptop").tryQuery()).isPresent();
        assertThat(robot.lookup("Java Programming").tryQuery()).isEmpty();
    }

    @Test
    void shouldFilterProductsByPriceRange() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("price between 40 and 100");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Products in price range 40-100
        assertThat(robot.lookup("Java Programming").tryQuery()).isPresent();  // 49.99
        assertThat(robot.lookup("Laptop").tryQuery()).isEmpty();              // 999.99
    }

    @Test
    void shouldFilterProductsByNameContains() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("products");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("name contains Java");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Products with 'Java' in name
        assertThat(robot.lookup("Java Programming").tryQuery()).isPresent();
        assertThat(robot.lookup("Laptop").tryQuery()).isEmpty();
    }

    // ===========================================
    // SEARCH FUNCTIONALITY
    // ===========================================

    @Test
    void shouldSearchAcrossAllFields() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Use global search (if available)
        robot.clickOn("Search");
        waitForUi();
        
        robot.write("alice");  // Should find in name field
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
        assertThat(robot.lookup("Charlie").tryQuery()).isEmpty();
    }

    @Test
    void shouldSearchCaseInsensitive() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Search");
        waitForUi();
        
        robot.write("ALICE");  // Uppercase search
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
    }

    @Test
    void shouldSearchByPartialMatch() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Search");
        waitForUi();
        
        robot.write("@example");  // Should find all users with @example in email
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    // ===========================================
    // FILTER MANAGEMENT
    // ===========================================

    @Test
    void shouldClearFilter() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply a filter
        robot.clickOn("Filter");
        waitForUi();
        robot.write("name = Alice");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Verify filter is applied
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
        
        // Clear the filter
        robot.clickOn("Clear Filter");  // or similar button
        waitForUi();
        
        // Verify all users are visible again
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldSaveAndLoadFilter() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        // Apply a complex filter
        robot.clickOn("Filter");
        waitForUi();
        robot.write("age > 25 AND email contains example");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Save the filter
        robot.clickOn("Save Filter");
        waitForUi();
        robot.write("Adults with Example Email");
        robot.clickOn("Save");
        waitForUi();
        
        // Clear and verify cleared
        robot.clickOn("Clear Filter");
        waitForUi();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();  // Should be visible again
        
        // Load the saved filter
        robot.clickOn("Load Filter");
        robot.clickOn("Adults with Example Email");
        waitForUi();
        
        // Verify filter is reapplied
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isEmpty();
    }

    // ===========================================
    // ERROR HANDLING AND VALIDATION
    // ===========================================

    @Test
    void shouldHandleInvalidFilterSyntax() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        // Invalid syntax
        robot.write("invalid syntax here @@@ bad");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Should show error or ignore invalid filter
        // All users should still be visible (filter not applied)
        assertThat(robot.lookup("Alice").tryQuery()).isPresent();
        assertThat(robot.lookup("Bob").tryQuery()).isPresent();
        assertThat(robot.lookup("Charlie").tryQuery()).isPresent();
    }

    @Test
    void shouldHandleFilterOnNonExistentField() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        robot.doubleClickOn("users");
        waitForUi();
        
        robot.clickOn("Filter");
        waitForUi();
        
        robot.write("nonexistent_field = value");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        // Should either show error or return no results
        // Verify graceful handling
    }

    // ===========================================
    // PERFORMANCE AND LARGE DATA FILTERING
    // ===========================================

    @Test
    void shouldFilterLargeDatasetEfficiently() throws InterruptedException {
        FxRobot robot = new FxRobot();
        setupLocalProfile(robot);
        
        // First, add many test users
        createLargeDataset();
        
        robot.doubleClickOn("users");
        waitForUi();
        
        long startTime = System.currentTimeMillis();
        
        robot.clickOn("Filter");
        waitForUi();
        robot.write("age > 50");
        robot.press(KeyCode.ENTER);
        waitForUi();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify filter completes in reasonable time (< 5 seconds)
        assertThat(duration).isLessThan(5000);
        
        // Verify filter works correctly
        // (specific assertions would depend on the test data)
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

    private void createAdditionalTestUsers() {
        // Add users with diverse data for filtering tests
        Map<String, AttributeValue> user4 = new HashMap<>();
        user4.put("id", new AttributeValue("4"));
        user4.put("name", new AttributeValue("David"));
        user4.put("email", new AttributeValue("david@test.com"));
        user4.put("age", new AttributeValue().withN("45"));
        user4.put("department", new AttributeValue("Engineering"));

        Map<String, AttributeValue> user5 = new HashMap<>();
        user5.put("id", new AttributeValue("5"));
        user5.put("name", new AttributeValue("Eve"));
        user5.put("email", new AttributeValue("eve@company.org"));
        user5.put("age", new AttributeValue().withN("28"));
        user5.put("department", new AttributeValue("Marketing"));

        try {
            dynamoDbClient.putItem("users", user4);
            dynamoDbClient.putItem("users", user5);
        } catch (Exception e) {
            // Ignore if table doesn't exist yet
        }
    }

    private void createAdditionalTestProducts() {
        Map<String, AttributeValue> product3 = new HashMap<>();
        product3.put("productId", new AttributeValue("PROD-003"));
        product3.put("category", new AttributeValue("Software"));
        product3.put("name", new AttributeValue("Database Tool"));
        product3.put("price", new AttributeValue().withN("199.99"));

        Map<String, AttributeValue> product4 = new HashMap<>();
        product4.put("productId", new AttributeValue("PROD-004"));
        product4.put("category", new AttributeValue("Electronics"));
        product4.put("name", new AttributeValue("Monitor"));
        product4.put("price", new AttributeValue().withN("299.99"));

        try {
            dynamoDbClient.putItem("products", product3);
            dynamoDbClient.putItem("products", product4);
        } catch (Exception e) {
            // Ignore if table doesn't exist yet
        }
    }

    private void createLargeDataset() {
        // Create 100 test users for performance testing
        for (int i = 100; i < 200; i++) {
            Map<String, AttributeValue> user = new HashMap<>();
            user.put("id", new AttributeValue(String.valueOf(i)));
            user.put("name", new AttributeValue("TestUser" + i));
            user.put("email", new AttributeValue("user" + i + "@test.com"));
            user.put("age", new AttributeValue().withN(String.valueOf(20 + (i % 60))));
            
            try {
                dynamoDbClient.putItem("users", user);
            } catch (Exception e) {
                // Continue on error
            }
        }
    }
}
