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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

/**
 * Singleton DynamoDB Local container to be shared across all E2E tests.
 * This improves test performance by reusing the same container instance.
 */
public class DynamoDbSingletonContainer {

    private static final GenericContainer<?> CONTAINER;

    static {
        CONTAINER = new GenericContainer<>("amazon/dynamodb-local:latest")
                .withExposedPorts(8000)
                .withCommand("-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
        
        System.out.println("Starting singleton DynamoDB Local container...");
        CONTAINER.start();
        System.out.println("Singleton DynamoDB Local container started successfully");
        
        // Add shutdown hook to ensure proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping singleton DynamoDB Local container...");
            CONTAINER.stop();
        }));
    }

    /**
     * Get the singleton DynamoDB container instance.
     * The container is started automatically when this class is first loaded.
     */
    public static GenericContainer<?> getInstance() {
        return CONTAINER;
    }

    /**
     * Get the DynamoDB endpoint URL.
     */
    public static String getEndpoint() {
        return "http://" + CONTAINER.getHost() + ":" + CONTAINER.getFirstMappedPort();
    }

    /**
     * Check if the container is running.
     */
    public static boolean isRunning() {
        return CONTAINER.isRunning();
    }

    // Private constructor to prevent instantiation
    private DynamoDbSingletonContainer() {
    }
}
