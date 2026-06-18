package com.breakinblocks.decoupled_time.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    @Shadow
    public abstract List<ServerPlayer> getPlayers();

    @Inject(method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onBroadcastAllGlobal(final Packet<?> packet, final CallbackInfo ci) {
        if (!(packet instanceof ClientboundSetTimePacket)) {
            return;
        }

        this.decoupledTime$broadcastDimensionSpecificTime();
        ci.cancel();
    }

    @Inject(method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/resources/ResourceKey;)V", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onBroadcastAllDimension(final Packet<?> packet, final ResourceKey<Level> dimension, final CallbackInfo ci) {
        if (!(packet instanceof ClientboundSetTimePacket)) {
            return;
        }

        this.decoupledTime$broadcastDimensionSpecificTime();
        ci.cancel();
    }

    private void decoupledTime$broadcastDimensionSpecificTime() {
        for (ServerPlayer player : this.getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) {
                continue;
            }

            final ClientboundSetTimePacket localPacket = new ClientboundSetTimePacket(
                    level.getGameTime(),
                    level.getDayTime(),
                    level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
            );
            player.connection.send(localPacket);
        }
    }
}
