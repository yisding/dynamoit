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

package ua.org.java.dynamoit.e2e.config;

/**
 * Configuration constants for E2E tests
 */
public final class E2ETestConfig {
    
    // Test data constants
    public static final String TEST_PROFILE_NAME = "e2e-test-profile";
    public static final String TEST_USER_ID_1 = "1";
    public static final String TEST_USER_ID_2 = "2";
    public static final String TEST_USER_ID_3 = "3";
    public static final String TEST_USER_NAME_1 = "Alice";
    public static final String TEST_USER_NAME_2 = "Bob";
    public static final String TEST_USER_NAME_3 = "Charlie";
    
    // Table names
    public static final String USERS_TABLE = "users";
    public static final String PRODUCTS_TABLE = "products";
    
    // Test timeouts (in milliseconds)
    public static final int UI_WAIT_TIMEOUT = 500;
    public static final int APP_START_TIMEOUT = 30000;
    public static final int TABLE_OPERATION_TIMEOUT = 5000;
    
    // Test data file paths
    public static final String TEST_DATA_PATH = "/test-data/";
    public static final String USERS_TEST_DATA = TEST_DATA_PATH + "users.json";
    public static final String PRODUCTS_TEST_DATA = TEST_DATA_PATH + "products.json";
    
    private E2ETestConfig() {
        // Utility class
    }
}
