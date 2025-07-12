#!/bin/bash

# Simple Mac visible mode test runner
set -e

echo "🍎 Testing DynamoIt in Mac Visible Mode"
echo "======================================="

# Check Docker
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running"
    exit 1
fi
echo "✅ Docker is running"

# Check OS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "⚠️  Not running on macOS - visible mode may not work optimally"
fi

echo "🧪 Running smoke test in visible mode..."

# Run the test with Mac-optimized settings
mvn test \
    -Dtest="SmokeE2ETest" \
    -Dtestfx.headless=false \
    -Djava.awt.headless=false \
    -Dtestfx.robot=glass \
    -Dapple.awt.application.name=DynamoIt_E2E_Test \
    -Dapple.laf.useScreenMenuBar=false \
    -Dprism.verbose=true \
    -Dtestfx.robot.write_sleep=300 \
    -Dtestfx.robot.key_sleep=300

echo ""
echo "🎉 Mac visible mode test completed!"
echo "💡 If you saw JavaFX windows appear, visible mode is working correctly"
