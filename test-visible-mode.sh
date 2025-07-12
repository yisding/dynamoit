#!/bin/bash

# Quick test script to validate visible mode configuration
set -e

echo "Testing Maven property configuration for visible mode..."

# Test headless mode (default)
echo ""
echo "=== Testing HEADLESS mode ==="
mvn help:evaluate -Dexpression=testfx.headless -q -DforceStdout
mvn help:evaluate -Dexpression=java.awt.headless -q -DforceStdout
mvn help:evaluate -Dexpression=glass.platform -q -DforceStdout
mvn help:evaluate -Dexpression=monocle.platform -q -DforceStdout

echo ""
echo "=== Testing VISIBLE mode (overridden) ==="
mvn help:evaluate -Dexpression=testfx.headless -Dtestfx.headless=false -q -DforceStdout
mvn help:evaluate -Dexpression=java.awt.headless -Djava.awt.headless=false -q -DforceStdout
mvn help:evaluate -Dexpression=glass.platform -Dglass.platform= -q -DforceStdout
mvn help:evaluate -Dexpression=monocle.platform -Dmonocle.platform= -q -DforceStdout

echo ""
echo "Property configuration test completed!"
