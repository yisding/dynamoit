# Building and Running

This page describes how to compile DynamoIt from source, create a distribution
and run it on different platforms.

## Prerequisites

- **Java 21** or newer must be installed and available on your `PATH`.
- **Maven 3.8+** is used to build the project and run tests.
- On Linux and macOS the `jpackage` tool is required for the optional portable
  bundle profile.

Check your versions with:

```bash
java -version
mvn -version
```

## Basic build

To compile the project and run the tests:

```bash
mvn clean test
```

A fat jar suitable for running with `java -jar` is produced with:

```bash
mvn clean package
```

The jar will be written to `target/fatJar/DynamoIt-<version>.jar`.

Run the application locally with:

```bash
java -jar target/fatJar/DynamoIt-<version>.jar
```

The application expects your AWS CLI profiles to be configured or you can create
local/remote profiles using the built‑in dialog.

## Portable bundles

For a self‑contained directory that includes the JRE, use the `package` Maven
profile:

```bash
mvn clean package -Ppackage
```

The resulting directory (or `.tar.gz` on Linux) will be placed in
`target/DynamoIt/`.

Platform specific icons are selected automatically using the `windows`, `linux`
or `mac` profiles which are triggered by Maven based on the detected OS.

## Running against DynamoDB Local

1. Start the [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html) server.
2. Create a **Local profile** in DynamoIt and point the endpoint to the local
   instance.
3. Open tables as usual; all operations will be executed against the local
   database.

## Logging

Logs are written to `~/.dynamoit/logs/`. If you encounter errors during startup
or connectivity issues, consult the latest log file in that directory.

