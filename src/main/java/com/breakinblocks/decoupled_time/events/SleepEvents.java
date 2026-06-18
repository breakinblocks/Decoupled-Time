package com.breakinblocks.decoupled_time.events;

import com.breakinblocks.decoupled_time.Decoupled_time;
import com.breakinblocks.decoupled_time.config.ModConfiguration;
import com.breakinblocks.decoupled_time.api.DerivedLevelDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

@EventBusSubscriber(modid = Decoupled_time.MODID)
public final class SleepEvents {
    private SleepEvents() {
        throw new IllegalStateException("Utility class");
    }

    @SubscribeEvent
    public static void onSleepFinished(final SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }

        if (!(level.getLevelData() instanceof DerivedLevelData derivedLevelData)) {
            return;
        }

        final DerivedLevelDataAccess accessor = (DerivedLevelDataAccess) derivedLevelData;
        final long currentDayTime = accessor.decoupledTime$getCustomDayTime();
        final long timeToMorning = (24000L - (currentDayTime % 24000L)) % 24000L;
        final long newDayTime = currentDayTime + timeToMorning;

        accessor.decoupledTime$setCustomDayTime(newDayTime);
        event.setTimeAddition(0L);
        level.setWeatherParameters(6000, 0, false, false);
    }
}
