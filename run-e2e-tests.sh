#!/bin/bash

# DynamoIt E2E Test Runner - Simplified Version
# This script provides convenient ways to run the simplified test suite

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 DynamoIt E2E Test Runner (Simplified)${NC}"
echo "================================="
echo ""
echo -e "${YELLOW}ℹ️  Note: Simplified E2E test suite with only essential tests.${NC}"
echo -e "${YELLOW}   For visible mode debugging on Mac, use the 'visible' option.${NC}"
echo ""

# Function to print usage
print_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  all              Run all E2E tests (smoke + creation)"
    echo "  smoke            Run smoke tests only"
    echo "  creation         Run item creation tests only"
    echo "  visible          Run tests in visible mode (for debugging)"
    echo "  visible-smoke    Run only smoke test in visible mode"
    echo "  visible-creation Run only creation test in visible mode"
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
    
    # Check for macOS specific JavaFX issues
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo -e "${YELLOW}ℹ️  macOS detected - ensuring optimal JavaFX configuration${NC}"
        # Check if we're running on Apple Silicon
        if [[ $(uname -m) == "arm64" ]]; then
            echo -e "${YELLOW}ℹ️  Apple Silicon detected - using compatible JavaFX settings${NC}"
        fi
    fi
}

# Check display environment for visible mode
check_display_environment() {
    if [ -n "$SSH_CLIENT" ] || [ -n "$SSH_TTY" ]; then
        echo -e "${RED}❌ SSH session detected - visible mode won't work${NC}"
        echo -e "${YELLOW}   Use headless mode instead or run locally${NC}"
        return 1
    fi
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo -e "${GREEN}✅ macOS display environment detected${NC}"
        echo -e "${YELLOW}ℹ️  Will configure for optimal Mac visible mode experience${NC}"
    fi
    
    return 0
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
        # Check display environment for visible mode
        if ! check_display_environment; then
            echo -e "${RED}❌ Cannot run in visible mode${NC}"
            exit 1
        fi
        
        # Mac-optimized visible mode settings
        if [[ "$OSTYPE" == "darwin"* ]]; then
            maven_props="-Dtestfx.headless=false -Djava.awt.headless=false -Dtestfx.robot=glass"
            maven_props="$maven_props -Dapple.awt.application.name=DynamoIt_E2E_Test"
            maven_props="$maven_props -Dapple.laf.useScreenMenuBar=false"
        else
            maven_props="-Dtestfx.headless=false -Djava.awt.headless=false -Dtestfx.robot=glass"
        fi
        
        echo -e "${YELLOW}👀 Running in VISIBLE mode for debugging${NC}"
        echo -e "${YELLOW}ℹ️  UI windows should appear during test execution${NC}"
        echo -e "${YELLOW}ℹ️  Tests will run slower to allow observation${NC}"
        
        # Add debugging properties and slower timing
        maven_props="$maven_props -Dprism.verbose=true -Dtestfx.robot.write_sleep=300 -Dtestfx.robot.key_sleep=300"
    else
        maven_props="-Dtestfx.headless=true -Dprism.order=sw -Djava.awt.headless=true -Dglass.platform=Monocle -Dmonocle.platform=Headless"
        echo -e "${GREEN}🔧 Running in HEADLESS mode${NC}"
    fi
    
    # Run the tests
    if mvn test -Dtest="$test_pattern" $maven_props $mvn_args; then
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
        run_test "SmokeE2ETest" "Smoke Tests (Infrastructure Verification)"
        ;;
    
    "creation")
        run_test "SimpleCreationE2ETest" "Simple Creation Tests"
        ;;
    
    "visible")
        echo -e "${YELLOW}� Running all tests in VISIBLE mode for debugging${NC}"
        run_test "SmokeE2ETest,SimpleCreationE2ETest" "All E2E Tests (Visible Mode)" true
        ;;
    
    "visible-smoke")
        echo -e "${YELLOW}🔍 Running smoke test in VISIBLE mode${NC}"
        run_test "SmokeE2ETest" "Smoke Test (Visible Mode)" true
        ;;
    
    "visible-creation")
        echo -e "${YELLOW}🔍 Running creation test in VISIBLE mode${NC}"
        run_test "SimpleCreationE2ETest" "Creation Test (Visible Mode)" true
        ;;
    
    "all")
        echo -e "${BLUE}Running simplified E2E test suite...${NC}"
        echo "📋 Test Categories:"
        echo "   - SmokeE2ETest: Infrastructure verification (3 tests)"
        echo "   - SimpleCreationE2ETest: Basic item creation (2 tests)"
        echo ""
        
        run_test "SmokeE2ETest,SimpleCreationE2ETest" "Complete Simplified E2E Test Suite (5 total tests)"
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
echo "• Run specific test: mvn test -Dtest='ClassName#methodName'"
echo "• Debug mode: Use 'visible' options to see UI interactions"
echo "• Mac users: Visible mode optimized for macOS display"
