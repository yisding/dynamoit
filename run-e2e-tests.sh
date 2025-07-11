#!/bin/bash

# DynamoIt E2E Test Runner
# This script sets up the environment and runs the E2E tests

set -e

echo "🚀 Starting DynamoIt E2E Test Suite"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"

# Check if Java 21 is available
if ! java -version 2>&1 | grep -q "21"; then
    echo "⚠️  Warning: Java 21 is recommended. Current version:"
    java -version
fi

# Set test environment variables
export TESTFX_HEADLESS=true
export PRISM_ORDER=sw
export JAVA_AWT_HEADLESS=true
export GLASS_PLATFORM=Monocle
export MONOCLE_PLATFORM=Headless

echo "🔧 Environment configured for headless testing"

# Run the E2E tests
echo "🧪 Running E2E tests..."

mvn clean test -Dtest="ua.org.java.dynamoit.e2e.**" -Dheadless=true

echo "✅ E2E tests completed!"

# Optional: Show test results
if [ -f "target/surefire-reports/TEST-*.xml" ]; then
    echo "📊 Test Results Summary:"
    grep -h "testcase\|testsuite" target/surefire-reports/TEST-*.xml | head -10
fi

echo "🎉 All done! Check target/surefire-reports/ for detailed results."
