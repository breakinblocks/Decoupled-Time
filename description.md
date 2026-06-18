# Decoupled Time

**Per-dimension time independence for NeoForge 1.21.1+**

## Summary

Decoupled Time lets each dimension run on its own clock. Dimensions no longer sync their day/night cycle to the Overworld — instead, each one tracks its own local time independently. Want permanent daylight in your team dimension while the Overworld cycles normally? Done.

## Description

By default, Minecraft forces every dimension to share the same day/night cycle. Decoupled Time breaks that link. Each configured dimension gets its own `dayTime` and `gameTime`, ticking independently from the rest of the server.

### What it does

- **Independent time per dimension** — Configure which dimensions run on their own clock using a simple whitelist or blacklist in the config. Supports wildcards (e.g. `modid:*`).
- **Configurable day length** — Make days longer or shorter with a single multiplier. Set it to `2.0` for 40-minute days, `0.5` for 10-minute days, or leave it at `1.0` for vanilla behavior.
- **Smart pausing** — Time only progresses in a dimension when players are actually there. Empty dimensions freeze until someone shows up. Spectators can optionally be excluded.
- **Dimension-aware time commands** — `/time` commands (`set`, `add`) accept an optional dimension argument, so you can manage each dimension's clock from anywhere.
- **Sleep stays local** — When players sleep, only the current dimension's time advances. Other dimensions keep ticking at their own pace.
- **Time sync on join** — Players receive the correct local time when they log in or change dimensions, so the sun is always where it should be.

### Who it's for

Server operators and modpack developers who want more control over how time works across dimensions. Particularly useful alongside mods that add custom dimensions (like FTB Teams) where a fixed time of day makes more sense than a cycling clock.

### Configuration

All settings live in `config/decoupled_time-common.toml`. No restarts required for most changes.
