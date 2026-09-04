# Bluemap_MedievalFactions

## Description

Bluemap_MedievalFactions is a Minecraft plugin that integrates [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) with [BlueMap](https://bluemap.bluecolored.de/), rendering faction land claims as coloured shape overlays on your BlueMap web map. Claimed chunks are merged into contiguous polygons per faction, updated automatically whenever a claim or unclaim event fires.

## Installation

### First Time Installation

1. Download the plugin jar from the [Releases](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/releases) page.
2. Place the jar in the `plugins` folder of your server.
3. Restart your server.

### Required Companion Plugins

This plugin requires the following plugins to be installed and enabled before it will load:

- [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) – the factions system whose claims are rendered.
- [BlueMap](https://www.spigotmc.org/resources/bluemap.83557/) – the web-map renderer that displays the overlays.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all commands
- [Configuration Guide](CONFIG.md) – Detailed configuration options

### Wiki & Additional Resources

- [Wiki](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/wiki)

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/issues/new).

- [Known Bugs](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)
- [Notes for Developers](https://github.com/Dans-Plugins/Bluemap_MedievalFactions/wiki)

## Testing

### Unit Tests

This project does not currently ship an automated test suite. There is no `src/test/`
directory and no test framework declared in `pom.xml`, so `mvn clean test` executes
zero tests — a `BUILD SUCCESS` from it verifies nothing.

Changes are currently verified by building the plugin and running it on a Paper server
with Medieval Factions and BlueMap installed.

## Development

### Build Prerequisite

Medieval Factions is declared in `pom.xml` as a `system`-scope dependency resolved from
`libs/medieval-factions-6.0.0-live-all.jar`. That directory is gitignored and the jar is not
distributed with this repository, so it must be supplied before any Maven goal will run.
See [CONTRIBUTING.md](CONTRIBUTING.md#build-prerequisite) for the steps.

### Building the Plugin

```
mvn clean package
```

Requires **JDK 21 or newer** — `bluemap-api` and `paper-api` ship Java 21 class files, so
a Java 17 compiler fails with `bad class file` even though the artifact targets Java 17.

The compiled jar will be placed in the `target/` directory.

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|--------------------|
| Kilz | Original author and primary developer |

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Project Status

This project is in active development.

### Changelog

See [CHANGELOG.md](CHANGELOG.md) for a release-by-release summary of changes.
