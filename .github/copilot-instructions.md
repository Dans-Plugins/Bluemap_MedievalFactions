# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java
- Build tool: Maven
- Target platform: Spigot / Paper (Minecraft plugin)
- Test framework: JUnit (via Maven Surefire)

## Project Structure

- `src/main/java/` – Plugin source code
- `src/main/resources/` – `plugin.yml` and `config.yml`
- `pom.xml` – Maven build descriptor

## Coding Conventions

- Follow the existing package structure (`com.kilz.mfbluemap`) when adding new classes.
- Annotate every event listener method with `@EventHandler` and `@Override` where applicable.
- Do not hard-code user-facing strings; use `config.yml` for configurable messages.
- Geometry operations use the JTS library (shaded into the plugin jar under `com.kilz.mfbluemap.shaded.jts`).
- Recompute tasks are debounced via `ScheduledExecutorService` to avoid redundant work on rapid events.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
