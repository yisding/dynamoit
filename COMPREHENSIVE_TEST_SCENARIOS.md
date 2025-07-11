# DynamoIt E2E Test Scenarios Documentation

This document provides a comprehensive overview of all End-to-End test scenarios implemented for DynamoIt, organized by functionality area.

## Test Class Overview

| Test Class | Purpose | Test Count | Coverage Area |
|------------|---------|------------|---------------|
| `SmokeE2ETest` | Infrastructure verification | 3 | Container, DB setup, system properties |
| `DynamoItMainE2ETest` | Core functionality | 6 | Basic UI operations, data display |
| `ProfileManagementE2ETest` | Profile operations | 5 | Profile CRUD, switching, validation |
| `TableOperationsE2ETest` | Table operations | 7 | Metadata, refresh, export, pagination |
| `CrudOperationsE2ETest` | CRUD operations | 18 | Create, Read, Update, Delete scenarios |
| `SearchAndFilterE2ETest` | Search & Filter | 25 | Filtering, search, complex queries |
| `AdvancedTableOperationsE2ETest` | Advanced features | 15 | Bulk ops, import/export, performance |

**Total Test Scenarios: 79**

---

## 1. Infrastructure & Smoke Tests (`SmokeE2ETest`)

### Infrastructure Verification
- [x] `shouldStartDynamoDbLocalContainer()` - Verify container startup
- [x] `shouldCreateAndQueryTestTables()` - Verify test data setup
- [x] `shouldHaveSystemPropertiesSet()` - Verify configuration

---

## 2. Core Application Tests (`DynamoItMainE2ETest`)

### Basic UI Operations
- [x] `shouldDisplayTablesInLocalProfile()` - Profile selection and table listing
- [x] `shouldOpenTableAndDisplayItems()` - Table opening and data display
- [x] `shouldEditItemAndPersistChanges()` - Basic item editing
- [x] `shouldFilterTableData()` - Simple filtering
- [x] `shouldCreateNewItem()` - Basic item creation
- [x] `shouldDeleteItem()` - Basic item deletion

---

## 3. Profile Management (`ProfileManagementE2ETest`)

### Profile Lifecycle
- [x] `shouldCreateAndUseLocalProfile()` - Profile creation and usage
- [x] `shouldEditExistingProfile()` - Profile modification
- [x] `shouldDeleteProfile()` - Profile removal
- [x] `shouldSwitchBetweenProfiles()` - Profile switching
- [x] `shouldHandleInvalidEndpoint()` - Error handling

---

## 4. Basic Table Operations (`TableOperationsE2ETest`)

### Table Management
- [x] `shouldDisplayTableMetadata()` - Schema and metadata display
- [x] `shouldRefreshTableData()` - Data refresh functionality
- [x] `shouldExportTableData()` - Basic export operations
- [x] `shouldHandleLargeTable()` - Large dataset handling
- [x] `shouldSortTableData()` - Column sorting
- [x] `shouldSearchTableData()` - Basic search functionality
- [x] `shouldCopyItemToClipboard()` - Copy operations

---

## 5. CRUD Operations (`CrudOperationsE2ETest`)

### Create Operations
- [x] `shouldCreateNewUserWithAllFields()` - Complete user creation
- [x] `shouldCreateNewUserWithMinimalFields()` - Minimal required fields
- [x] `shouldCreateNewProductWithCompositeKey()` - Composite key handling
- [x] `shouldHandleCreateWithDuplicateKey()` - Duplicate key validation

### Update Operations
- [x] `shouldUpdateSingleFieldInUser()` - Single field updates
- [x] `shouldUpdateMultipleFieldsInUser()` - Multiple field updates
- [x] `shouldUpdateProductWithCompositeKey()` - Composite key updates

### Delete Operations
- [x] `shouldDeleteSingleUser()` - Single item deletion
- [x] `shouldDeleteMultipleUsers()` - Multiple item selection and deletion
- [x] `shouldDeleteProductWithCompositeKey()` - Composite key deletion
- [x] `shouldCancelDelete()` - Deletion cancellation

### Bulk Operations
- [x] `shouldCreateMultipleUsersInSequence()` - Sequential creation
- [x] `shouldValidateRequiredFields()` - Field validation
- [x] `shouldHandleSpecialCharactersInData()` - Unicode and special characters

---

## 6. Search and Filter Operations (`SearchAndFilterE2ETest`)

### Basic Filtering
- [x] `shouldFilterByExactMatch()` - Exact value matching
- [x] `shouldFilterByContains()` - Substring matching
- [x] `shouldFilterByBeginsWith()` - Prefix matching
- [x] `shouldFilterByNotEquals()` - Inequality filtering
- [x] `shouldFilterByNotContains()` - Negative substring matching

### Numeric Filtering
- [x] `shouldFilterByNumericGreaterThan()` - Greater than comparisons
- [x] `shouldFilterByNumericLessThan()` - Less than comparisons
- [x] `shouldFilterByNumericBetween()` - Range filtering

### Existence Filtering
- [x] `shouldFilterByAttributeExists()` - Attribute presence
- [x] `shouldFilterByAttributeNotExists()` - Attribute absence

### Complex Filtering
- [x] `shouldFilterWithMultipleConditionsAnd()` - AND logic
- [x] `shouldFilterWithMultipleConditionsOr()` - OR logic
- [x] `shouldFilterWithParentheses()` - Parentheses grouping

### Product Table Filtering
- [x] `shouldFilterProductsByCategory()` - Category-based filtering
- [x] `shouldFilterProductsByPriceRange()` - Price range filtering
- [x] `shouldFilterProductsByNameContains()` - Name-based filtering

### Search Functionality
- [x] `shouldSearchAcrossAllFields()` - Global search
- [x] `shouldSearchCaseInsensitive()` - Case-insensitive search
- [x] `shouldSearchByPartialMatch()` - Partial matching

### Filter Management
- [x] `shouldClearFilter()` - Filter clearing
- [x] `shouldSaveAndLoadFilter()` - Filter persistence

### Error Handling
- [x] `shouldHandleInvalidFilterSyntax()` - Syntax error handling
- [x] `shouldHandleFilterOnNonExistentField()` - Invalid field handling

### Performance
- [x] `shouldFilterLargeDatasetEfficiently()` - Large dataset performance

---

## 7. Advanced Table Operations (`AdvancedTableOperationsE2ETest`)

### Advanced Insertion
- [x] `shouldInsertItemWithNestedObjects()` - Complex nested data
- [x] `shouldInsertItemWithListAttributes()` - Array/list data
- [x] `shouldInsertItemWithLargeTextData()` - Large text handling

### Bulk Operations
- [x] `shouldImportDataFromClipboard()` - Clipboard import
- [x] `shouldBatchInsertMultipleItems()` - Batch insertion
- [x] `shouldBatchDeleteMultipleItems()` - Batch deletion
- [x] `shouldDeleteByFilter()` - Filter-based deletion

### Advanced Search
- [x] `shouldSearchWithRegularExpressions()` - Regex search
- [x] `shouldSearchAcrossMultipleTables()` - Cross-table search
- [x] `shouldSaveSearchQueries()` - Query persistence

### Data Export
- [x] `shouldExportTableToCSV()` - CSV export
- [x] `shouldExportFilteredData()` - Filtered data export
- [x] `shouldExportToMultipleFormats()` - Multiple format support

### Performance & Stress Testing
- [x] `shouldHandleLargeVolumeInsert()` - High-volume insertion
- [x] `shouldHandleConcurrentOperations()` - Concurrent operation handling

### Error Handling
- [x] `shouldHandleInsertionErrors()` - Data validation errors
- [x] `shouldHandleNetworkInterruption()` - Network failure simulation

---

## Test Data Scenarios

### User Data Patterns
- **Basic Users**: Alice (30), Bob (25), Charlie (35)
- **Extended Users**: David (45, Engineering), Eve (28, Marketing)
- **Bulk Users**: 100+ users for performance testing
- **Special Characters**: Unicode, emojis, special symbols
- **Edge Cases**: Empty fields, very long text, invalid formats

### Product Data Patterns
- **Electronics**: Laptop ($999.99), Monitor ($299.99)
- **Books**: Java Programming ($49.99)
- **Software**: Database Tool ($199.99)
- **Composite Keys**: productId + category combinations

### Filter Test Patterns
- **Simple**: Single field, exact match
- **Complex**: Multiple conditions with AND/OR
- **Numeric**: Range queries, comparisons
- **Text**: Contains, begins with, regex
- **Existence**: Field presence/absence

---

## Performance Benchmarks

### Expected Performance Targets
- **UI Response**: < 500ms for basic operations
- **Filter Application**: < 2s for 1000+ records
- **Bulk Insert**: < 30s for 100 records
- **Export Operations**: < 10s for standard datasets
- **Search Operations**: < 1s for typical queries

### Stress Test Scenarios
- **Large Dataset**: 1000+ records
- **Complex Queries**: Multiple nested conditions
- **Concurrent Operations**: Simultaneous UI actions
- **Memory Usage**: Large text fields and nested objects

---

## Error Scenarios Covered

### Data Validation
- Required field validation
- Data type validation
- Duplicate key handling
- Invalid character handling

### Network Issues
- Connection timeouts
- Service unavailability
- Partial operation failures

### UI Edge Cases
- Rapid user interactions
- Invalid input handling
- Memory constraints
- Display limitations

---

## Test Execution Strategies

### Individual Test Classes
```bash
# Run specific test class
mvn test -Dtest="CrudOperationsE2ETest"
mvn test -Dtest="SearchAndFilterE2ETest"
```

### Test Categories
```bash
# Basic functionality tests
mvn test -Dtest="SmokeE2ETest,DynamoItMainE2ETest"

# Advanced feature tests  
mvn test -Dtest="SearchAndFilterE2ETest,AdvancedTableOperationsE2ETest"
```

### Performance Tests
```bash
# Performance-focused tests
mvn test -Dtest="*E2ETest" -Dtest.groups="performance"
```

### Full Test Suite
```bash
# All E2E tests
./run-e2e-tests.sh
```

---

## Maintenance and Updates

### Adding New Test Scenarios
1. **Identify functionality area** - Choose appropriate test class
2. **Follow naming conventions** - Use descriptive test method names
3. **Include setup/teardown** - Use helper methods for data setup
4. **Add database verification** - Always verify persistence
5. **Update documentation** - Add to this document

### Test Data Management
- **Base data**: Defined in `DynamoItE2ETestBase`
- **Extended data**: Added per test class as needed
- **Cleanup**: Automatic via container lifecycle
- **Isolation**: Each test class gets fresh data

### Performance Monitoring
- **Execution time tracking** - Built into critical tests
- **Memory usage monitoring** - Via JVM metrics
- **Failure rate tracking** - CI/CD integration
- **Trend analysis** - Historical performance data

This comprehensive test suite provides thorough coverage of DynamoIt's functionality, ensuring reliability and performance across all user scenarios.
