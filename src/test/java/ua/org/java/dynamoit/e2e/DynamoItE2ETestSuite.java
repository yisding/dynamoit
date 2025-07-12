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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for the simplified DynamoIt End-to-End tests.
 * 
 * Run this suite to execute all E2E tests in the correct order.
 * Each test class is self-contained and sets up its own test data.
 */
@Suite
@SelectClasses({
    SmokeE2ETest.class,
    SimpleCreationE2ETest.class
})
public class DynamoItE2ETestSuite {
    // This class is used only as a holder for the above annotations
}
