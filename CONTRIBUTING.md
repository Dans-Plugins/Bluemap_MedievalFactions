# Contributing

## Thank You

Thank you for your interest in contributing to Bluemap_MedievalFactions! This guide will help you get started.

## Links

- [Website](https://dansplugins.com)
- [Discord](https://discord.gg/xXtuAQ2)

## Requirements

- A GitHub account
- Git installed on your local machine
- A JDK. **Java 21 or newer is required to build**, even though the artifact targets Java 17: `bluemap-api` and `paper-api` ship Java 21 class files, and a Java 17 compiler rejects them with `bad class file`.
- A Java IDE or text editor
- A basic understanding of Java

## Getting Started

1. [Sign up for GitHub](https://github.com/signup) if you don't have an account.
2. Fork the repository by clicking **Fork** at the top right of the repo page.
3. Clone your fork: `git clone https://github.com/<your-username>/Bluemap_MedievalFactions.git`
4. Open the project in your IDE.
5. Supply the Medieval Factions jar (see [Build Prerequisite](#build-prerequisite) below) — the build cannot resolve its dependencies without it.
6. Build the plugin: `mvn clean package`
   If you encounter errors, please open an issue.

## Build Prerequisite

`pom.xml` declares Medieval Factions as a `system`-scope dependency resolved from a
path inside the clone:

```xml
<systemPath>${project.basedir}/libs/medieval-factions-6.0.0-live-all.jar</systemPath>
```

`libs/` is listed in `.gitignore` and the jar is not distributed with this repository,
so it must be supplied before any Maven goal will run. Medieval Factions' rolling `dev`
release provides a suitable build:

```bash
mkdir -p libs
gh release download dev -R Dans-Plugins/Medieval-Factions \
  -D libs -p '*-all.jar' --clobber
mv libs/medieval-factions-*-all.jar libs/medieval-factions-6.0.0-live-all.jar
```

The filename must match the `systemPath` above; Maven resolves it by path, not by
coordinate. The rename is required because **no published Medieval Factions artifact
carries the version string `6.0.0-live`** — the `dev` asset is named for its build date.

Without that file, `mvn clean package` fails during dependency resolution with
`Could not find artifact com.dansplugins:medievalfactions:jar:6.0.0-live`. No repository
declaration replaces this step: neither `com.dansplugins:medievalfactions` against the
repositories declared in `pom.xml` nor `com.github.Dans-Plugins:Medieval-Factions`
against JitPack resolves that coordinate.

CI performs exactly these steps — see the `Fetch the MedievalFactions API jar` step in
[`.github/workflows/build.yml`](.github/workflows/build.yml), which is the authoritative
copy if this section drifts.

## Identifying What to Work On

### Issues

Work items are tracked as [GitHub issues](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/issues).

### Milestones

Issues are grouped into [milestones](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/milestones) representing upcoming releases.

## Making Changes

1. Make sure an issue exists for the work. If not, create one.
2. Switch to `master`: `git checkout master`
3. Create a branch: `git checkout -b <branch-name>`
4. Make your changes.
5. Test your changes.
6. Commit: `git commit -m "Description of changes"`
7. Push: `git push origin <branch-name>`
8. Open a pull request against `master`, link the related issue with `#<number>`.
9. Address review feedback.

`master` is the only branch in this repository; there is no `main` or `develop` branch.

## Testing

Run the test suite with:

Linux: `mvn clean test`
Windows: `mvn.cmd clean test`

Tests live in `src/test/java/com/kilz/mfbluemap/`, run on JUnit 5 via Maven Surefire,
and are named `<ClassUnderTest>Test.java`. Read the Surefire summary rather than the
build status — `BUILD SUCCESS` with `Tests run: 0` means nothing was verified. The
suite needs the Medieval Factions jar like any other Maven goal here; see
[Build Prerequisite](#build-prerequisite).

The suite covers only what runs without a server: `ClaimGeometry` (merging claimed
chunks into polygons) and `FactionColors` (parsing and deriving overlay colours).
`BlueMapIntegration` cannot be instantiated in a test, because its constructor calls
`Bukkit.getPluginManager().registerEvents(...)`; covering it would need a Bukkit
mocking framework that this project does not yet depend on. Verify changes to it by
building the plugin and running it on a Paper server with Medieval Factions and
BlueMap installed:

Linux: `mvn clean package`
Windows: `mvn.cmd clean package`

Pure logic that a new change adds is expected to come with tests. Where that means
lifting logic out of `BlueMapIntegration` first, that extraction is welcome.

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).
