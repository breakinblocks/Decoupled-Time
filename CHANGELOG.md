# Changelog

All notable changes to Decoupled Time will be documented in this file.

## [1.1.0]

### Added

- **InControl Compatibility:** Added full compatibility with [InControl](https://www.curseforge.com/minecraft/mc-mods/in-control) (McJty) for dimension-specific spawn rules. InControl's `time`, `daycount`, and `phase` conditions now respect per-dimension time in decoupled dimensions.
  - **`time` / `mintime` / `maxtime`:** Already worked via existing `DerivedLevelData` mixin — no additional fix needed.
  - **`daycount` / `mindaycount` / `maxdaycount`:** Redirected from the global Overworld day counter to a per-dimension value computed from each dimension's own `dayTime`.
  - **Phase system (`phases.json`):** Phase rules are now evaluated independently for each decoupled dimension every 10 ticks. Spawn rules that require specific phases will see the correct phase state for the dimension they are evaluating in.
  - Compatibility is **fully optional** — mixins load only when InControl is present via a custom `IMixinConfigPlugin`. No crash or log spam if InControl is absent.
- **`DecoupledTimeAPI`:** New public utility class exposing `getDimensionDayTime(Level)`, `getDimensionTime(Level)`, `getDimensionDayCount(Level)`, and `isDimensionDecoupled(Level)` for use by other mods and compatibility layers.

## [1.0.3]

### Added

- **Time Phase Events:** Added `DimensionTimePhaseEvent` fired on the NeoForge event bus when a decoupled dimension's day time crosses into a new phase (`DAY`, `NOON`, `NIGHT`, `MIDNIGHT`). Enables other mods (e.g. KubeJS) to hook into time-of-day transitions per dimension.

  **KubeJS Example:**
  ```js
    NativeEvents.onEvent("com.breakinblocks.decoupled_time.api.DimensionTimePhaseEvent", (event) => {
        const {phase, level, dayTime} = event;
        switch (phase.name()) {
            case "DAY":
                // do something when day starts in this dimension
                break;
            case "NOON":
                // do something when noon starts in this dimension
                break;
            case "NIGHT":
                // do something when night starts in this dimension
                break;
            case "MIDNIGHT":
                // do something when midnight starts in this dimension
                break;
        }
    });
  ```

## [1.0.2]

### Changed

- **Split Smart Pausing Config:** `enableSmartPausing` has been replaced with two independent toggles: `pauseGameTime` and `pauseDayTime`. This allows pausing the day/night cycle independently from gameTime (scheduled ticks, entity processing) when no active players are present.

## [1.0.1]

### Fixed

- **Sun/Moon Jitter with Day Length Multiplier:** Fixed rubber-banding of the sun and moon when using non-default `dayLengthMultiplier` values. The client was advancing dayTime at vanilla rate (+1/tick) between server syncs, causing visual snapping. Added client-side mixin to match the server's fractional advancement rate.
- **Time Commands Nesting:** Fixed `/time add` and `/time query` being incorrectly nested under `/time set` instead of being top-level subcommands.
- **Time Commands on Non-Decoupled Dimensions:** Time commands no longer error on non-decoupled dimensions. They now fall back to the global level time and label output with `(global)`.

## [1.0.0]

### Added

- **Per-Dimension Time Decoupling:** Each dimension maintains its own independent `dayTime` and `gameTime`, configurable via whitelist/blacklist.
- **Smart Pausing:** Local time progression pauses when no active players are present in a dimension (configurable).
- **Dimension-Aware Time Commands:** `/time set` and `/time add` now accept an optional dimension argument.
- **Sleep Synchronization:** When players sleep, only the current dimension's time advances.
- **Time Sync on Join:** Optional time packet sync on login and dimension change.
- **Day Length Multiplier:** Configurable `dayLengthMultiplier` to make days longer or shorter per decoupled dimension (e.g., `2.0` = 40 minute days, `0.5` = 10 minute days).
- **Spectator Pause Option:** Spectators can optionally not count as active players for smart pause.
