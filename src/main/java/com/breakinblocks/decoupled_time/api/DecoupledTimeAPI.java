package com.breakinblocks.decoupled_time.api;

import com.breakinblocks.decoupled_time.config.ModConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Public API for querying per-dimension time values.
 *
 * When a dimension is decoupled, these methods return the dimension's own
 * independent time. For non-decoupled dimensions they fall through to the
 * vanilla (Overworld-inherited) values.
 */
public final class DecoupledTimeAPI {

    private DecoupledTimeAPI() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns the day-time for the given level. For decoupled dimensions this
     * is the dimension's own day-time; otherwise it is the vanilla value.
     */
    public static long getDimensionDayTime(Level level) {
        return level.getDayTime();
    }

    /**
     * Returns the game-time for the given level. For decoupled dimensions this
     * is the dimension's own game-time; otherwise it is the vanilla value.
     */
    public static long getDimensionTime(Level level) {
        return level.getGameTime();
    }

    /**
     * Computes the day-count (number of completed day/night cycles) for a
     * level. Equivalent to {@code getDimensionDayTime(level) / 24000}.
     */
    public static int getDimensionDayCount(Level level) {
        return (int) (getDimensionDayTime(level) / 24000L);
    }

    /**
     * Returns {@code true} when the given dimension has its own independent
     * time-line according to the current configuration.
     */
    public static boolean isDimensionDecoupled(Level level) {
        ResourceLocation dimId = level.dimension().location();
        return ModConfiguration.isDimensionDecoupled(dimId);
    }
}
