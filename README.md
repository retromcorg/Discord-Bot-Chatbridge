# Discord-Bot-Chatbridge
A Discord Chat Bridge that uses https://github.com/RhysB/Discord-Bot-Core

## Build
This project now uses Maven.

Build with:
`mvn clean package`

All plugin dependencies resolve from the JohnyMuffin Nexus repository and are `provided` scope, so the
built jar contains only this plugin's own classes.

This version targets **Project Poseidon 2.0.0** and **DiscordCore-6** (JDA 6). It is not compatible with
Poseidon 1.x or the older `DiscordCore` plugin.


## Libraries (Name and URL)

1. **Discord Bot Core**
    - GitHub: [https://github.com/RhysB/Discord-Bot-Core](https://github.com/RhysB/Discord-Bot-Core)

2. **Project Poseidon**
    - GitHub: [https://github.com/RhysB/Project-Poseidon](https://github.com/RhysB/Project-Poseidon)

3. **RetroBridge**
    - GitHub: [https://github.com/retromcorg/RetroBridge](https://github.com/retromcorg/RetroBridge)


# Setup
1. Install DiscordCore-6 (https://github.com/RhysB/Discord-Bot-Core)
2. Configure Bot Core with Token
3. Install Discord-Bot-Chatbridge and configure Servername and Discord Channel ID

Optional:
Enable `retrobridge-prefix-support.enabled` if you want Discord messages to include player prefixes exposed through RetroBridge.

Restarting will be required to generate configs.
