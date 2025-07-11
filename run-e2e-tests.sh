#!/bin/bash

# DynamoIt E2E Test Runner
# This script provides convenie    # Configure Maven properties based on visible mode
    local maven_props=""
    if [ "$visible_mode" = "true" ]; then
        maven_props="-Dtestfx.headless=false -Djava.awt.headless=false"
        echo -e "${YELLOW}👀 Running in VISIBLE mode for debugging${NC}"
    else
        maven_props="-Dtestfx.headless=true -Dprism.order=sw -Djava.awt.headless=true -Dglass.platform=Monocle -Dmonocle.platform=Headless"
        echo -e "${GREEN}🔧 Running in HEADLESS mode${NC}"
    fio run different test suites

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 DynamoIt E2E Test Runner${NC}"
echo "================================="
echo ""
echo -e "${YELLOW}ℹ️  Note: E2E tests are excluded from regular 'mvn test' runs for performance.${NC}"
echo -e "${YELLOW}   Use this script or specify tests explicitly to run E2E tests.${NC}"
echo -e "${YELLOW}   For visible mode debugging, use the 'visible' option.${NC}"
echo ""

# Function to print usage
print_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  all              Run all E2E tests (default)"
    echo "  smoke            Run smoke tests only"
    echo "  basic            Run basic functionality tests"
    echo "  crud             Run CRUD operation tests"
    echo "  search           Run search and filter tests"
    echo "  advanced         Run advanced operation tests"
    echo "  profile          Run profile management tests"
    echo "  table            Run table operation tests"
    echo "  performance      Run performance-focused tests"
    echo "  visible          Run tests in visible mode (for debugging)"
    echo "  help             Show this help message"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker is running${NC}"
}

# Check Java version
check_java() {
    if ! java -version 2>&1 | grep -q "21"; then
        echo -e "${YELLOW}⚠️  Warning: Java 21 is recommended. Current version:${NC}"
        java -version
    fi
}

# Setup test environment
setup_environment() {
    echo -e "${GREEN}🔧 Environment configured for test execution${NC}"
    echo -e "${YELLOW}ℹ️  Test mode (headless/visible) will be configured per test run${NC}"
}

# Function to run tests with proper configuration
run_test() {
    local test_pattern=$1
    local description=$2
    local visible_mode=${3:-false}
    local mvn_args=${4:-""}
    
    echo -e "${YELLOW}🧪 Running $description...${NC}"
    
    # Configure Maven properties based on visible mode
    local maven_props=""
    if [ "$visible_mode" = "true" ]; then
        maven_props="-Dtestfx.headless=false -Djava.awt.headless=false -Dglass.platform= -Dmonocle.platform="
        echo -e "${YELLOW}👀 Running in VISIBLE mode for debugging${NC}"
    else
        maven_props="-Dtestfx.headless=true -Dprism.order=sw -Djava.awt.headless=true -Dglass.platform=Monocle -Dmonocle.platform=Headless"
        echo -e "${GREEN}� Running in HEADLESS mode${NC}"
    fi
    
    # Run the tests
    if mvn test -Dtest="$test_pattern" $maven_props -q $mvn_args; then
        echo -e "${GREEN}✅ $description completed successfully${NC}"
    else
        echo -e "${RED}❌ $description failed${NC}"
        exit 1
    fi
}

# Show test results summary
show_results() {
    if ls target/surefire-reports/TEST-*.xml >/dev/null 2>&1; then
        echo -e "${BLUE}📊 Test Results Summary:${NC}"
        local total_tests=$(grep -h "tests=" target/surefire-reports/TEST-*.xml 2>/dev/null | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        local failures=$(grep -h "failures=" target/surefire-reports/TEST-*.xml 2>/dev/null | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        local errors=$(grep -h "errors=" target/surefire-reports/TEST-*.xml 2>/dev/null | sed 's/.*errors="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
        
        echo "Total Tests: ${total_tests:-0}"
        echo "Failures: ${failures:-0}"
        echo "Errors: ${errors:-0}"
    else
        echo -e "${YELLOW}📊 Test results not found in expected location${NC}"
    fi
}

# Pre-flight checks
check_docker
check_java
setup_environment

# Parse command line arguments
case "${1:-all}" in
    "smoke")
        run_test "SmokeE2ETest" "Smoke Tests"
        ;;
    
    "basic")
        run_test "SmokeE2ETest,DynamoItMainE2ETest" "Basic Functionality Tests"
        ;;
    
    "crud")
        run_test "CrudOperationsE2ETest" "CRUD Operation Tests"
        ;;
    
    "search")
        run_test "SearchAndFilterE2ETest" "Search and Filter Tests"
        ;;
    
    "advanced")
        run_test "AdvancedTableOperationsE2ETest" "Advanced Operation Tests"
        ;;
    
    "profile")
        run_test "ProfileManagementE2ETest" "Profile Management Tests"
        ;;
    
    "table")
        run_test "TableOperationsE2ETest" "Table Operation Tests"
        ;;
    
    "performance")
        echo -e "${YELLOW}🏃 Running Performance Tests...${NC}"
        echo "Note: This may take several minutes"
        run_test "*E2ETest#shouldHandleLargeVolumeInsert,*E2ETest#shouldHandleConcurrentOperations,*E2ETest#shouldFilterLargeDatasetEfficiently" "Performance Tests"
        ;;
    
    "visible")
        echo -e "${YELLOW}� Running all tests in VISIBLE mode for debugging${NC}"
        run_test "ua.org.java.dynamoit.e2e.**" "All E2E Tests (Visible Mode)" true
        ;;
    
    "all")
        echo -e "${BLUE}Running complete E2E test suite...${NC}"
        echo "�📋 Test Categories:"
        echo "   - SmokeE2ETest: Infrastructure verification"
        echo "   - DynamoItMainE2ETest: Core application functionality"
        echo "   - ProfileManagementE2ETest: Profile CRUD operations"
        echo "   - TableOperationsE2ETest: Basic table operations"
        echo "   - CrudOperationsE2ETest: Comprehensive CRUD scenarios (18 tests)"
        echo "   - SearchAndFilterE2ETest: Advanced filtering and search (25 tests)"
        echo "   - AdvancedTableOperationsE2ETest: Bulk operations and edge cases (15 tests)"
        echo ""
        
        run_test "ua.org.java.dynamoit.e2e.**" "Complete E2E Test Suite (79 total scenarios)" false "-De2e.parallel=true"
        ;;
    
    "help"|"-h"|"--help")
        print_usage
        exit 0
        ;;
    
    *)
        echo -e "${RED}Unknown option: $1${NC}"
        echo ""
        print_usage
        exit 1
        ;;
esac

echo ""
show_results

echo -e "${GREEN}🎉 Test execution completed!${NC}"

# Additional information
echo ""
echo -e "${BLUE}📁 Additional Resources:${NC}"
echo "• Detailed results: target/surefire-reports/"
echo "• Test documentation: COMPREHENSIVE_TEST_SCENARIOS.md"
echo "• Run specific test: mvn test -Dtest='ClassName#methodName'"
echo "• Debug mode: Use 'visible' option to see UI interactions"
echo "• E2E tests are excluded from 'mvn test' - use this script instead"
