package com.breakinblocks.decoupled_time.events;

import com.breakinblocks.decoupled_time.Decoupled_time;
import com.breakinblocks.decoupled_time.config.ModConfiguration;
import com.breakinblocks.decoupled_time.data.DimensionTimeManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = Decoupled_time.MODID)
public final class DimensionDataEvents {
    private DimensionDataEvents() {
        throw new IllegalStateException("Utility class");
    }

    @SubscribeEvent
    public static void onLevelLoad(final LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }

        DimensionTimeManager.initializeFromStorage(level);
    }

    @SubscribeEvent
    public static void onLevelSave(final LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }

        DimensionTimeManager.flushToStorage(level);
    }

    @SubscribeEvent
    public static void onLevelUnload(final LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }

        DimensionTimeManager.flushToStorage(level);
    }
}
