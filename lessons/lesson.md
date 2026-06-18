# Lessons Learned

## DimensionType codec extension on NeoForge 1.21.1

### Before (1.21.1 vanilla)
`DimensionType.DIRECT_CODEC` is built via `RecordCodecBuilder` in static init with no `timelines` field.

### After (1.21.1 backport)
Replaced `DIRECT_CODEC` in `MixinDimensionType` at `<clinit>` tail and rebuilt `CODEC` via `RegistryFileCodec.create(Registries.DIMENSION_TYPE, DIRECT_CODEC)`.

```java
@Inject(method = "<clinit>", at = @At("TAIL"))
private static void decoupled_time$injectTimelinesIntoCodec(CallbackInfo ci) {
    DIRECT_CODEC = ExtraCodecs.catchDecoderException(RecordCodecBuilder.create(...));
    CODEC = RegistryFileCodec.create(Registries.DIMENSION_TYPE, DIRECT_CODEC);
}
```

## Timeline schema modeling

### Before (1.21.1)
No native timeline classes/codecs in this project.

### After (backport)
Added records + codecs:
- `Keyframe(ticks, value)`
- `AttributeTrack(ease, modifier, keyframes)`
- `Timelines(periodTicks, tracks)`

Implemented early validation for invalid/unsorted keyframes and invalid `period_ticks`.
