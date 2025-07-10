# Architecture Overview

This document explains the key modules and runtime flow of DynamoIt. It is intended for
developers who want to understand how the application is structured and where to add new
functionality.

## Layers

```
Launcher -> DynamoItApp -> Dagger AppFactory
              |                |
              |                +-- Main view and ThemeManager
              |                +-- DynamoDBService
              +-- JavaFX Application thread
```

1. **Launcher** starts the JVM, sets up logging and delegates to `DynamoItApp`.
2. **DynamoItApp** is the JavaFX `Application` implementation. In `start()` it constructs
   a `DaggerAppFactory` which wires together all Dagger modules.
3. **AppFactory** exposes two main objects:
   - the root JavaFX `Region` returned by `MainModule`
   - the `ThemeManager` used to switch between dark and light themes.

## Dagger modules

The dependency injection graph is defined using [Dagger](https://dagger.dev). The
important modules are:

| Module | Purpose |
|--------|---------|
| `MainModule` | Provides the `EventBus`, main view/model, `HostServices` and the main controller. |
| `DynamoDBModule` | Produces a singleton `DynamoDBService` that creates AWS SDK clients. |
| `ThemeManagerModule` | Creates the `ThemeManager` singleton. |
| `ProfileModule` | Supplies per-profile models used by the profile viewer. |
| `TableGridModule` | Supplies controller, model and view for the table grid. |

Each component (`ProfileComponent`, `TableGridComponent`) has a `@Component.Builder` that
binds runtime values such as the selected profile or table context.

## EventBus

`EventBus` is a small publish/subscribe helper built on RxJava. It tracks asynchronous
operations so that the `ActivityIndicator` can display a spinner while tasks run. The bus
also exposes an observable for the currently selected table. UI controllers publish
updates to this bus and listen for changes to react accordingly.

Typical usage:

```java
eventBus.activity(service.loadItems())
        .thenAccept(items -> model.setItems(items));
```

Exceptions are routed to `ExceptionDialog` so that errors are surfaced on the UI thread.

## UI components

UI views are implemented programmatically in Java (no FXML files). Each major feature is a
component consisting of a model, controller and view class.

- **Main view** hosts the toolbar, profile selector and dynamic tab pane.
- **Profile viewer** lists tables for a profile and allows creating new profiles.
- **Table grid** shows items for a table, supports filtering and editing and displays
  table metadata via `TableInfoDialog`.

Components are built using factory methods on `MainController` which create the relevant
Dagger component for that feature. This ensures each opened table has its own isolated
model and controller instances.

## DynamoDBService

`DynamoDBService` centralises interaction with the AWS SDK. It lazily creates
`AmazonDynamoDB` and `DynamoDB` clients per profile. `getListOfTables` retrieves table
names via pagination. The service understands three profile types:

1. Preconfigured AWS CLI profiles
2. Local profiles that point to a DynamoDB Local endpoint
3. Remote profiles with explicit access keys

Both synchronous and asynchronous methods are provided so that controllers can offload
long-running tasks onto background threads using `CompletableFuture`.

## Threading model

Background tasks use Java's `CompletableFuture` API. The `EventBus` ensures that UI changes
are executed on the JavaFX application thread via `FXExecutor`. This keeps controllers
simple while avoiding race conditions when updating the view.

