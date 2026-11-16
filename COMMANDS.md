# Commands Reference

Bluemap_MedievalFactions is a background integration plugin. It does not register any player-facing or admin commands.

All functionality — syncing faction claims to the BlueMap overlay, updating markers on claim, unclaim and disband events, and applying colour overrides — operates automatically without any commands.

## Configuration

To customise the plugin's behaviour, edit `plugins/MF_Bluemap/config.yml`, then either restart the server or run BlueMap's own `/bluemap reload`. This plugin registers no reload command of its own, but it re-reads its configuration every time BlueMap starts, and that includes after a `/bluemap reload`. Every faction is then redrawn with the new settings. See [CONFIG.md](CONFIG.md) for the full list of options.
