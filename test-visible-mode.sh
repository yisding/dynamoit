#!/bin/bash

# Quick test script to validate visible mode configuration on Mac
set -e

echo "Testing Maven property configuration for visible mode (Mac optimized)..."

# Test headless mode (default)
echo ""
echo "=== Testing HEADLESS mode ==="
echo "testfx.headless: $(mvn help:evaluate -Dexpression=testfx.headless -q -DforceStdout 2>/dev/null)"
echo "java.awt.headless: $(mvn help:evaluate -Dexpression=java.awt.headless -q -DforceStdout 2>/dev/null)"
echo "glass.platform: $(mvn help:evaluate -Dexpression=glass.platform -q -DforceStdout 2>/dev/null)"
echo "monocle.platform: $(mvn help:evaluate -Dexpression=monocle.platform -q -DforceStdout 2>/dev/null)"

echo ""
echo "=== Testing VISIBLE mode (Mac optimized) ==="
echo "testfx.headless: $(mvn help:evaluate -Dexpression=testfx.headless -Dtestfx.headless=false -q -DforceStdout 2>/dev/null)"
echo "java.awt.headless: $(mvn help:evaluate -Dexpression=java.awt.headless -Djava.awt.headless=false -q -DforceStdout 2>/dev/null)"
echo "testfx.robot: $(mvn help:evaluate -Dexpression=testfx.robot -Dtestfx.robot=glass -q -DforceStdout 2>/dev/null)"
echo "apple.awt.application.name: $(mvn help:evaluate -Dexpression=apple.awt.application.name -Dapple.awt.application.name=DynamoIt_E2E_Test -q -DforceStdout 2>/dev/null)"

echo ""
echo "=== System Information ==="
echo "OS: $(uname -s)"
echo "Architecture: $(uname -m)"
echo "Java Version: $(java -version 2>&1 | head -n 1)"

echo ""
echo "Property configuration test completed!"
echo ""
echo "To test visible mode:"
echo "  ./run-e2e-tests.sh visible-smoke"
echo "  ./run-e2e-tests.sh visible-creation"
