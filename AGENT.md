# DynamoIt Codebase Guide

## Build/Test Commands
- `mvn clean package` - Build application (creates target/fatJar/DynamoIt-<version>.jar)
- `mvn test` - Run unit tests (excludes E2E tests)
- `mvn test -Dtest="ClassName#methodName"` - Run specific test
- `./run-e2e-tests.sh all` - Run complete E2E test suite
- `./run-e2e-tests.sh smoke` - Run infrastructure verification tests only
- `./run-e2e-tests.sh visible` - Run E2E tests in visible mode for debugging
- `mvn clean package -Ppackage` - Build portable application bundle

## Architecture
- **JavaFX Desktop Application**: DynamoDB graphical client with Maven build
- **Main Entry**: `ua.org.java.dynamoit.Launcher` -> `DynamoItApp`
- **Dependency Injection**: Dagger 2 framework (`AppFactory`)
- **Testing**: JUnit 4/5, TestFX, Testcontainers for DynamoDB Local
- **Key Dependencies**: AWS SDK v1, JavaFX 24, RxJava 2, Jackson
- **Test Structure**: Unit tests in src/test/java, E2E tests with headless/visible modes

## Code Style
- **Headers**: GPL v3 license header on all files
- **Packages**: `ua.org.java.dynamoit.*` pattern
- **Naming**: PascalCase classes, camelCase methods/variables
- **Error Handling**: Try-catch with specific exception types, ExceptionDialog for UI errors
- **Imports**: Group by java.*, external libs, internal packages
- **Constants**: UPPER_SNAKE_CASE static finals
- **Utils**: Static utility classes (e.g., Utils.OBJECT_MAPPER for JSON)
