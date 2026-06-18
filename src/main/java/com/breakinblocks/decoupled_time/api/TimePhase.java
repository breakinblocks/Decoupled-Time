package com.breakinblocks.decoupled_time.api;

/**
 * Represents the four phases of a Minecraft day cycle (24000 ticks).
 * Each phase starts at a specific tick offset within the day.
 */
public enum TimePhase {
    DAY(0),
    NOON(6000),
    NIGHT(12000),
    MIDNIGHT(18000);

    private final long startTick;

    TimePhase(final long startTick) {
        this.startTick = startTick;
    }

    public long getStartTick() {
        return this.startTick;
    }

    /**
     * Returns the phase that the given dayTime falls into.
     */
    public static TimePhase fromDayTime(final long dayTime) {
        final long tickInDay = ((dayTime % 24000L) + 24000L) % 24000L;
        if (tickInDay >= MIDNIGHT.startTick) return MIDNIGHT;
        if (tickInDay >= NIGHT.startTick) return NIGHT;
        if (tickInDay >= NOON.startTick) return NOON;
        return DAY;
    }
}