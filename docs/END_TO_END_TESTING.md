Below is a recipe that teams have found reliable when they need **true end-to-end (E2E) coverage** for a desktop JavaFX client that talks to DynamoDB—in your case, *Dynamoit*.

---

## 1. Choose the right “test double” for DynamoDB

| Option                                                | When to use                                                                                | How to wire it in a test                                                                                                                                                                                                                                                                                     |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **DynamoDB Local**<br>(official AWS jar/Docker image) | Fast, single-node, 100 % API fidelity, no extra AWS services needed.                       | Spin it up once per test class (JUnit 5 `@TestInstance(PER_CLASS)`) or via **Testcontainers** so it’s auto-started and torn down. ([AWS Documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html))                                           |
| **LocalStack**                                        | You already depend on other AWS mocks (S3, SQS…) or want to exercise IAM-style auth flows. | Use `@Container` (`LocalStackContainer`) and enable only the services you need (`withServices(DYNAMODB)`). ([AWS Builder Center](https://community.aws/content/2dxWQAZsdc3dk5uCILAmNqEME2e/testing-dynamodb-interactions-in-spring-boot-using-localstack-and-testcontainers?lang=en&utm_source=chatgpt.com)) |
| **Embedded clones (Dynalite / Dynategra)**            | You need ultra-fast, in-JVM tests (no Docker) and are OK with partial feature support.     | `DynaliteContainer` from Testcontainers Java. ([Testcontainers for Java](https://java.testcontainers.org/modules/databases/dynalite/?utm_source=chatgpt.com))                                                                                                                                                |

**Tip (Endpoint override)**
When Dynamoit starts up in a test, inject `-Daws.dynamodb.endpoint=http://<host>:<port>` or call `DynamoDbClient.builder().endpointOverride(...)` so the app silently points at the container.

---

## 2. Automate the JavaFX UI with TestFX

* **API fluency & assertions.** TestFX gives you a robot DSL (`clickOn()`, `write()`, etc.) and Hamcrest/AssertJ matchers. ([GitHub](https://github.com/TestFX/TestFX?utm_source=chatgpt.com))
* **Headless on CI.** Add Monocle jars and VM flags so tests run on GitHub Actions/TeamCity without X-server 👉 `-Dtestfx.headless=true -Dprism.order=sw -Djava.awt.headless=true` (or `-Dglass.platform=Monocle -Dmonocle.platform=Headless` for Linux). TestFX documents Monocle as the supported headless backend. ([GitHub](https://github.com/TestFX/TestFX))

```xml
<!-- maven-surefire plugin snippet -->
<configuration>
  <argLine>
    -Dtestfx.headless=true
    -Dprism.order=sw
    -Djava.awt.headless=true
  </argLine>
</configuration>
```

---

## 3. Glue it together ─ sample JUnit 5 + Testcontainers fixture

```java
@Testcontainers
@ExtendWith(ApplicationExtension.class)
class DynamoitE2E {

  // 1️⃣  spin up DynamoDB Local in Docker
  static final GenericContainer<?> dynamo =
      new GenericContainer<>("amazon/dynamodb-local:latest")
          .withExposedPorts(8000)
          .withCommand("-jar DynamoDBLocal.jar -sharedDb");

  @Start
  void start(Stage stage) throws Exception {
      String endpoint = "http://" + dynamo.getHost() + ":" + dynamo.getFirstMappedPort();
      System.setProperty("aws.dynamodb.endpoint", endpoint);

      // seed schema & data once
      DynamoDbClient client =
          DynamoDbClient.builder().endpointOverride(URI.create(endpoint))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("fake", "fake")))
                        .build();
      createTablesAndTestData(client);

      // 2️⃣  launch Dynamoit
      new DynamoitApp().start(stage);
  }

  @Test
  void editItemReflectsInDatabase(FxRobot robot) {
      robot.clickOn("#profileCombo").clickOn("local");
      robot.doubleClickOn("users");          // open table
      robot.doubleClickOn("Alice");          // open edit dialog
      robot.write("Alice-QA"); robot.type(KeyCode.ENTER);

      // 3️⃣  verify persisted in DynamoDB
      Map<String,AttributeValue> item = client.getItem(b -> b
              .tableName("users")
              .key(Map.of("id", AttributeValue.builder().s("1").build())))
          .item();
      assertThat(item.get("name").s()).isEqualTo("Alice-QA");
  }
}
```

*All layers (UI robot ➜ Dynamoit controllers ➜ AWS SDK calls ➜ local DB) are executed, so the test fails only on real integration faults.*

---

## 4. Seed and clean data deterministically

* Create tables at **class-level** (`@BeforeAll`) and delete them in `@AfterAll`.
* Use **unique table names** per test run—prepend a `UUID` or the container port—to avoid parallel-suite clashes.
* Keep fixtures in JSON and load them through the AWS CLI or SDK before the UI starts, e.g.:

```bash
aws dynamodb batch-write-item \
  --request-items file://seed/users.json \
  --endpoint-url "$DYNAMO_ENDPOINT"
```

---

## 5. Layered assertions

| Layer            | Why                                                       | Example                                               |
| ---------------- | --------------------------------------------------------- | ----------------------------------------------------- |
| **UI-state**     | Confirms that the scene graph renders what the user sees. | `verifyThat("#statusLabel", hasText("Saved"))`        |
| **Domain model** | Captures business intent regardless of visuals.           | Check table row count after add-row.                  |
| **Database**     | Guarantees persistence contract.                          | Use DynamoDB client to assert item exists (see code). |

---

## 6. Run it in CI/CD

* **GitHub Actions**:

  ```yaml
  services:
    docker:
      image: amazon/dynamodb-local:latest
      ports: ['8000:8000']
  ```

  or rely on Testcontainers’ \[+ Ryuk] auto-clean-up.

* **Memory & graphics**: allocate at least 2 GB RAM and set `-Dprism.order=sw` to force software rendering when no GPU is available.

---

## 7. Keep testability in Dynamoit itself

1. **MVVM or MVP** architecture so controllers can be constructed with *explicit* dependencies (e.g., `DynamoDbClient`), making them easy to swap for mocks.
2. **Feature flags** (system properties/env vars) for choosing *remote* vs *local* DynamoDB.
3. **Avoid Thread.sleep**—use JavaFX `Platform.runLater` and TestFX `WaitForAsyncUtils` to synchronise with the FX thread.

---

### Checklist to adopt

* [ ] TestFX 4.x dependency and Monocle jars added
* [ ] DynamoDB Local / LocalStack container scripted via Testcontainers
* [ ] Tables seeded and torn down in test lifecycle
* [ ] UI + database assertions in every test
* [ ] Headless flags set in Maven/Gradle CI profile
* [ ] Dynamoit’s data-access code accepts endpoint override (for local DB)

With this setup you’ll have **repeatable, infrastructure-free E2E tests** that open your real JavaFX windows, drive them like a user, and verify the bytes persisted in DynamoDB—without ever hitting AWS.
