# DynamoIt Developer Guide

This document provides high‑level guidance for contributors. The application is a
JavaFX based graphical client for Amazon DynamoDB that allows browsing, editing
and maintaining tables through a desktop interface. For a deep dive into the
codebase see [Architecture Overview](architecture.md) and for build instructions
see [Building and Running](build-and-run.md).

## Project layout

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── ua/org/java/dynamoit
│   │   └── resources
│   └── test
└── docs
```

- **Launcher** – entry point that configures logging and starts `DynamoItApp`.
- **DynamoItApp** – JavaFX `Application` that builds the main scene using `Dagger`.
- **AppFactory** – Dagger component wiring together modules such as the main view, DynamoDB client layer and the theme manager.

Source code is organised around *components* (Main view, Profile viewer, Table grid, Theme manager) with their own controllers, models and views. Reusable widgets and utilities live under `widgets` and `utils`.

## Building and running

This guide only covers the basics. See [Building and Running](build-and-run.md)
for detailed instructions on setting up a development environment and packaging
the application.

## Dependency injection

The project uses [Dagger](https://dagger.dev/) for dependency injection. Modules such as `MainModule`, `DynamoDBModule` and `ThemeManagerModule` define providers for controllers, services and views. `AppFactory` composes these modules to bootstrap the UI:

```java
@Component(modules = {MainModule.class, DynamoDBModule.class, ThemeManagerModule.class})
@Singleton
public interface AppFactory {
    Region mainView();
    ThemeManager themeManager();
}
```

New components should follow the same pattern – a Dagger `Component` with a builder that binds required runtime values (for example, `ProfileComponent` binds the selected profile).

## UI components

- **Main view** (`components/main`) – Hosts the toolbar, profile selector and tab pane. `MainController` creates subcomponents for profile browsing and table grids.
- **Profile viewer** (`components/profileviewer`) – Lists tables for a specific AWS profile. It loads table names asynchronously via `DynamoDBService` and allows saving table filters.
- **Table grid** (`components/tablegrid`) – Displays table items, supports filtering, editing and pagination. `TableGridController` encapsulates DynamoDB interactions.
- **Theme manager** (`components/thememanager`) – Applies the light/dark themes provided by `Atlantafx`.

The `EventBus` provides simple communication between components and also tracks asynchronous activity for the UI `ActivityIndicator`.

## DynamoDB integration

`DynamoDBService` wraps creation of AWS SDK clients based on profile information. It supports three profile types:

- Preconfigured AWS CLI profiles (region taken from the config)
- Local profiles for a DynamoDB local endpoint
- Remote profiles where access keys and region are provided manually

Each opened profile caches DynamoDB and Document API clients for reuse.

## Testing

Unit tests reside under `src/test/java` and can be executed with:

```bash
mvn test
```

The tests cover the filter parser logic, utility methods and selected UI controllers.

## Contributing

1. Fork the repository and create a feature branch.
2. Ensure `mvn test` passes before submitting a pull request.
3. Keep code style consistent with existing files (spaces for indentation and GPLv3 headers).

For further reference, consult the sources of the individual components and existing tests.

