package com.breakinblocks.decoupled_time.mixin;

import com.breakinblocks.decoupled_time.api.DimensionTimePhaseEvent;
import com.breakinblocks.decoupled_time.api.TimePhase;
import com.breakinblocks.decoupled_time.config.ModConfiguration;
import com.breakinblocks.decoupled_time.data.DimensionTimeManager;
import com.breakinblocks.decoupled_time.api.DerivedLevelDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    private static final Map<net.minecraft.resources.ResourceKey<?>, Double> decoupledTime$dayTimeAccumulator = new HashMap<>();
    private static final Map<net.minecraft.resources.ResourceKey<?>, TimePhase> decoupledTime$lastPhase = new HashMap<>();

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onTickTime(final CallbackInfo ci) {
        final ServerLevel level = (ServerLevel) (Object) this;

        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }
        if (!(level.getLevelData() instanceof DerivedLevelData derivedLevelData)) {
            return;
        }

        final DerivedLevelDataAccess accessor = (DerivedLevelDataAccess) derivedLevelData;
        accessor.decoupledTime$setDecoupled(true);

        final boolean hasActivePlayers = this.decoupledTime$hasActivePlayers(level.players());
        final boolean pauseGameTime = ModConfiguration.PAUSE_GAME_TIME.get() && !hasActivePlayers;
        final boolean pauseDayTime = ModConfiguration.PAUSE_DAY_TIME.get() && !hasActivePlayers;

        if (!pauseGameTime) {
            accessor.decoupledTime$setCustomGameTime(accessor.decoupledTime$getCustomGameTime() + 1L);
        }

        if (!pauseDayTime && level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            final double multiplier = ModConfiguration.DAY_LENGTH_MULTIPLIER.get();
            final double rate = 1.0 / multiplier;
            final var dimensionKey = level.dimension();
            final double accumulated = decoupledTime$dayTimeAccumulator.getOrDefault(dimensionKey, 0.0) + rate;
            final long wholeTicks = (long) accumulated;
            decoupledTime$dayTimeAccumulator.put(dimensionKey, accumulated - wholeTicks);
            if (wholeTicks > 0) {
                accessor.decoupledTime$setCustomDayTime(accessor.decoupledTime$getCustomDayTime() + wholeTicks);
            }
        }

        final long currentDayTime = accessor.decoupledTime$getCustomDayTime();
        final TimePhase currentPhase = TimePhase.fromDayTime(currentDayTime);
        final var dimensionKeyForPhase = level.dimension();
        final TimePhase previousPhase = decoupledTime$lastPhase.get(dimensionKeyForPhase);
        if (previousPhase != currentPhase) {
            decoupledTime$lastPhase.put(dimensionKeyForPhase, currentPhase);
            if (previousPhase != null) {
                NeoForge.EVENT_BUS.post(new DimensionTimePhaseEvent(level, currentPhase, currentDayTime));
            }
        }

        if (!pauseGameTime && (accessor.decoupledTime$getCustomGameTime() & 127L) == 0L) {
            DimensionTimeManager.flushToStorage(level);
        }

        ci.cancel();
    }

    private boolean decoupledTime$hasActivePlayers(final List<ServerPlayer> players) {
        if (players.isEmpty()) {
            return false;
        }
        if (!ModConfiguration.PAUSE_IN_SPECTATOR.get()) {
            return true;
        }

        for (ServerPlayer player : players) {
            if (!player.isSpectator()) {
                return true;
            }
        }
        return false;
    }
}
