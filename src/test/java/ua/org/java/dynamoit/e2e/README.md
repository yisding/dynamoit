# DynamoIt End-to-End (E2E) Testing

This directory contains the end-to-end test suite for DynamoIt, which tests the complete application stack from the UI down to the DynamoDB database.

## Overview

The E2E tests use:
- **TestFX** for UI automation and assertions
- **DynamoDB Local** running in Docker via Testcontainers for database operations
- **Monocle** for headless JavaFX testing
- **JUnit 5** as the testing framework

## Test Structure

```
src/test/java/ua/org/java/dynamoit/e2e/
├── DynamoItE2ETestBase.java          # Base class with common setup
├── DynamoItMainE2ETest.java          # Main application functionality tests
├── ProfileManagementE2ETest.java     # Profile management tests
├── TableOperationsE2ETest.java       # Table operation tests
├── DynamoItE2ETestSuite.java         # Test suite runner
└── config/
    └── E2ETestConfig.java            # Test configuration constants
```

## Running the Tests

### Prerequisites

1. **Docker** must be installed and running
2. **Java 21** or later
3. **Maven 3.6+**

### Local Development

#### Option 1: Use the provided script
```bash
./run-e2e-tests.sh
```

#### Option 2: Run with Maven directly
```bash
mvn clean test -Dtest="ua.org.java.dynamoit.e2e.**"
```

#### Option 3: Run individual test classes
```bash
# Run main functionality tests
mvn test -Dtest="DynamoItMainE2ETest"

# Run profile management tests
mvn test -Dtest="ProfileManagementE2ETest"

# Run table operations tests
mvn test -Dtest="TableOperationsE2ETest"
```

### CI/CD

The tests run automatically in GitHub Actions using the workflow defined in `.github/workflows/e2e-tests.yml`.

## Test Data

Test data is managed in JSON files under `src/test/resources/test-data/`:
- `users.json` - Sample user data
- `products.json` - Sample product data

The base test class automatically:
1. Starts a DynamoDB Local container
2. Creates test tables (`users`, `products`)
3. Populates them with sample data
4. Configures DynamoIt to use the local endpoint

## Writing New E2E Tests

### 1. Extend the Base Class

```java
class MyNewE2ETest extends DynamoItE2ETestBase {
    
    @Test
    void shouldDoSomething() throws InterruptedException {
        FxRobot robot = new FxRobot();
        
        // Your test code here
        robot.clickOn("Button");
        waitForUi();
        
        assertThat(robot.lookup("Result").tryQuery()).isPresent();
    }
}
```

### 2. Use TestFX Robot API

Common TestFX operations:
```java
// Click operations
robot.clickOn("Button Text");
robot.clickOn("#buttonId");
robot.doubleClickOn("Table Item");
robot.rightClickOn("Context Menu Item");

// Keyboard operations
robot.write("text input");
robot.press(KeyCode.ENTER);
robot.press(KeyCode.CONTROL, KeyCode.A);

// Waiting
waitForUi(); // Custom helper method
robot.sleep(Duration.ofMillis(500));

// Assertions
assertThat(robot.lookup("Element").tryQuery()).isPresent();
assertThat(robot.lookup("Missing").tryQuery()).isEmpty();
```

### 3. Database Verification

Verify changes persist to the database:
```java
// Get item from database
Map<String, AttributeValue> key = new HashMap<>();
key.put("id", new AttributeValue("1"));
Map<String, AttributeValue> item = getItemFromDb("users", key);

assertThat(item).isNotNull();
assertThat(item.get("name").getS()).isEqualTo("Expected Name");
```

## Troubleshooting

### Common Issues

1. **Tests fail with "Application failed to start"**
   - Ensure Docker is running
   - Check that no other processes are using port 8000
   - Verify JavaFX modules are available

2. **UI elements not found**
   - Add more `waitForUi()` calls
   - Check element selectors (use ScenicView for debugging)
   - Ensure the UI has fully loaded before interaction

3. **Headless mode issues**
   - Verify Monocle dependencies are on classpath
   - Check that all required system properties are set
   - Try running with visible display first for debugging

### Debugging

#### Enable visible mode for debugging:
```bash
mvn test -Dtest="DynamoItMainE2ETest" -Dtestfx.headless=false
```

#### View the DynamoDB Local logs:
The Docker container logs are available in test output when using Testcontainers.

#### Use ScenicView for UI debugging:
Add this dependency for development:
```xml
<dependency>
    <groupId>org.scenicview</groupId>
    <artifactId>scenicview</artifactId>
    <version>11.0.2</version>
    <scope>test</scope>
</dependency>
```

## Configuration

### Environment Variables

- `TESTFX_HEADLESS=true` - Run tests in headless mode
- `PRISM_ORDER=sw` - Use software rendering
- `JAVA_AWT_HEADLESS=true` - Enable AWT headless mode

### System Properties

- `testfx.headless=true` - TestFX headless mode
- `glass.platform=Monocle` - JavaFX headless platform
- `monocle.platform=Headless` - Monocle headless mode

## Best Practices

1. **Test Isolation**: Each test should be independent and not rely on state from other tests
2. **Wait for UI**: Always wait for UI operations to complete before assertions
3. **Database Verification**: Verify both UI state and database persistence
4. **Descriptive Names**: Use clear, descriptive test method names
5. **Page Object Pattern**: Consider extracting common UI interactions into helper methods
6. **Data Cleanup**: The base class handles cleanup, but clean up any additional test data

## Performance Tips

1. **Container Reuse**: Testcontainers reuses the DynamoDB Local container across tests
2. **Parallel Execution**: Tests run sequentially to avoid UI conflicts
3. **Selective Testing**: Run specific test classes during development
4. **Optimized Waits**: Use appropriate wait times (not too short, not too long)

## Contributing

When adding new E2E tests:

1. Follow the existing patterns in the test classes
2. Add new tests to the appropriate test class or create a new one
3. Update this README if you add new testing patterns or utilities
4. Ensure tests pass both locally and in CI
5. Consider the test execution time and optimize where possible
