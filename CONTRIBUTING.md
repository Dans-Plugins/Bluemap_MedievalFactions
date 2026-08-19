# Contributing

## Thank You

Thank you for your interest in contributing to Bluemap_MedievalFactions! This guide will help you get started.

## Links

- [Website](https://dansplugins.com)
- [Discord](https://discord.gg/xXtuAQ2)

## Requirements

- A GitHub account
- Git installed on your local machine
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
<systemPath>${project.basedir}/libs/medieval-factions-5.6.0-all.jar</systemPath>
```

`libs/` is listed in `.gitignore` and the jar is not distributed with this repository,
so it must be supplied out of band before any Maven goal will run. Download
`medieval-factions-5.6.0-all.jar` from the
[Medieval Factions releases](https://github.com/Dans-Plugins/Medieval-Factions/releases)
page, then place it at `libs/medieval-factions-5.6.0-all.jar` relative to the repository root:

```
mkdir libs
# copy the downloaded jar into libs/ as medieval-factions-5.6.0-all.jar
```

Without that file, `mvn clean package` fails during dependency resolution with
`Could not find artifact com.dansplugins:medievalfactions:jar:5.6.0`. The artifact is
not published to Maven Central or JitPack, so no repository declaration can replace
this step.

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

This project does not currently have an automated test suite — there is no
`src/test/` directory and no test framework declared in `pom.xml`. `mvn clean test`
therefore executes zero tests, and a `BUILD SUCCESS` from it is not evidence that
anything was verified.

Until a test suite exists, verify changes by building the plugin and running it on a
Paper server with Medieval Factions and BlueMap installed:

Linux: `mvn clean package`
Windows: `mvn.cmd clean package`

Adding test infrastructure is welcome; see [Build Prerequisite](#build-prerequisite)
for what is needed to make any Maven goal run at all.

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).
