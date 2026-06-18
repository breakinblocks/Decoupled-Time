package com.breakinblocks.decoupled_time.api;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge event bus when a decoupled dimension's day time
 * crosses into a new {@link TimePhase}.
 *
 * <p>This event is not cancellable and has no result.</p>
 *
 * <p>Useful for other mods (e.g. KubeJS) to hook into time-of-day transitions
 * on a per-dimension basis.</p>
 */
public class DimensionTimePhaseEvent extends Event {
    private final ServerLevel level;
    private final TimePhase phase;
    private final long dayTime;

    public DimensionTimePhaseEvent(final ServerLevel level, final TimePhase phase, final long dayTime) {
        this.level = level;
        this.phase = phase;
        this.dayTime = dayTime;
    }

    /**
     * The dimension whose time phase changed.
     */
    public ServerLevel getLevel() {
        return this.level;
    }

    /**
     * The new phase that just started.
     */
    public TimePhase getPhase() {
        return this.phase;
    }

    /**
     * The exact dayTime tick when the transition was detected.
     */
    public long getDayTime() {
        return this.dayTime;
    }
}