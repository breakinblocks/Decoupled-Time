package com.breakinblocks.decoupled_time.events;

import com.breakinblocks.decoupled_time.Decoupled_time;
import com.breakinblocks.decoupled_time.network.TimeSyncDispatcher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Decoupled_time.MODID)
public final class TimeSyncEvents {
    private TimeSyncEvents() {
        throw new IllegalStateException("Utility class");
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        TimeSyncDispatcher.sendCurrentLevelTime(player, level);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        TimeSyncDispatcher.sendCurrentLevelTime(player, level);
    }
}
