package com.breakinblocks.decoupled_time.network;

import com.breakinblocks.decoupled_time.config.ModConfiguration;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public final class TimeSyncDispatcher {
    private TimeSyncDispatcher() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendCurrentLevelTime(final ServerPlayer player, final ServerLevel level) {
        if (!ModConfiguration.SYNC_TIME_ON_JOIN.get()) {
            return;
        }

        final ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
                level.getGameTime(),
                level.getDayTime(),
                level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
        );
        player.connection.send(packet);
    }
}
