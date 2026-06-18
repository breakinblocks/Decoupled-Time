# Decoupled Time Backport PRD (1.21.1)

## Status
In Progress

## Major Systems Checklist
- [x] Parse `dimension_type.json` `timelines` object on 1.21.1
- [x] Add typed timeline records (`Timelines`, `AttributeTrack`, `Keyframe`)
- [x] Add DFU codecs for timeline records
- [x] Extend `DimensionType` with timeline storage (`IDimensionTypeExtensions`)
- [x] Inject `timelines` optional field into `DimensionType.DIRECT_CODEC`
- [ ] Use parsed timeline in `MixinClientLevel` sun/moon interpolation
- [ ] Verify multiplayer sync path for dimension-specific timeline usage

## Known Roadblocks
1. `DimensionType.DIRECT_CODEC` is initialized in static initializer and does not expose extension hooks on 1.21.1.
2. `timelines.ease` supports richer forms upstream (e.g. custom easing object) but this implementation currently targets the string form needed for immediate backport work.
3. Additional schema additions after 1.21.11 (e.g. `clock`, `time_markers`) are intentionally out of scope for this step.
