package com.breakinblocks.decoupled_time.commands;

import com.breakinblocks.decoupled_time.api.DerivedLevelDataAccess;
import com.breakinblocks.decoupled_time.config.ModConfiguration;
import com.breakinblocks.decoupled_time.data.DimensionTimeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;

public final class TimeCommandDimensionSupport {
    private TimeCommandDimensionSupport() {
        throw new IllegalStateException("Utility class");
    }

    public static int setTimeInDimension(final CommandSourceStack source, final ServerLevel level, final int time) {
        applyDayTime(level, time);

        final boolean decoupled = ModConfiguration.isDimensionDecoupled(level.dimension().location());
        final String label = decoupled ? "Set time" : "Set time (global)";
        final int dayTime = normalizeDayTime(level.getDayTime());
        source.sendSuccess(
                () -> Component.literal(label + " in " + level.dimension().location() + " to " + dayTime),
                true
        );
        return dayTime;
    }

    public static int addTimeInDimension(final CommandSourceStack source, final ServerLevel level, final int amount) {
        applyDayTime(level, level.getDayTime() + amount);

        final boolean decoupled = ModConfiguration.isDimensionDecoupled(level.dimension().location());
        final String label = decoupled ? "Set time" : "Set time (global)";
        final int dayTime = normalizeDayTime(level.getDayTime());
        source.sendSuccess(
                () -> Component.literal(label + " in " + level.dimension().location() + " to " + dayTime),
                true
        );
        return dayTime;
    }

    public static int queryDayTimeInDimension(final CommandSourceStack source, final ServerLevel level) {
        final boolean decoupled = ModConfiguration.isDimensionDecoupled(level.dimension().location());
        final String label = decoupled ? "Daytime" : "Daytime (global)";
        final int dayTime = (int)(level.getDayTime() % 24000L);
        source.sendSuccess(
                () -> Component.literal(label + " in " + level.dimension().location() + " is " + dayTime),
                true
        );
        return dayTime;
    }

    public static int queryGameTimeInDimension(final CommandSourceStack source, final ServerLevel level) {
        final boolean decoupled = ModConfiguration.isDimensionDecoupled(level.dimension().location());
        final String label = decoupled ? "Game time" : "Game time (global)";
        final int gameTime = (int)(level.getGameTime() % 2147483647L);
        source.sendSuccess(
                () -> Component.literal(label + " in " + level.dimension().location() + " is " + gameTime),
                true
        );
        return gameTime;
    }

    public static int queryDayInDimension(final CommandSourceStack source, final ServerLevel level) {
        final boolean decoupled = ModConfiguration.isDimensionDecoupled(level.dimension().location());
        final String label = decoupled ? "Day" : "Day (global)";
        final int day = (int)(level.getDayTime() / 24000L % 2147483647L);
        source.sendSuccess(
                () -> Component.literal(label + " in " + level.dimension().location() + " is " + day),
                true
        );
        return day;
    }

    private static int normalizeDayTime(final long dayTime) {
        return (int) (dayTime % 24000L);
    }

    private static void applyDayTime(final ServerLevel level, final long dayTime) {
        if (level.getLevelData() instanceof DerivedLevelData derivedLevelData) {
            final DerivedLevelDataAccess accessor = (DerivedLevelDataAccess) derivedLevelData;
            accessor.decoupledTime$setDecoupled(true);
            accessor.decoupledTime$setCustomDayTime(dayTime);
            DimensionTimeManager.flushToStorage(level);
            return;
        }

        level.setDayTime(dayTime);
    }
}
