package com.breakinblocks.decoupled_time.mixin;

import com.breakinblocks.decoupled_time.config.ModConfiguration;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {
    @Unique
    private double decoupledTime$clientDayTimeAccumulator;

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onTickTime(final CallbackInfo ci) {
        final ClientLevel level = (ClientLevel) (Object) this;

        if (!ModConfiguration.isDimensionDecoupled(level.dimension().location())) {
            return;
        }

        final double multiplier = ModConfiguration.DAY_LENGTH_MULTIPLIER.get();
        if (multiplier == 1.0) {
            return;
        }

        // Advance gameTime normally (1 per tick)
        level.setGameTime(level.getGameTime() + 1L);

        // Advance dayTime at the fractional rate matching the server
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            final double rate = 1.0 / multiplier;
            this.decoupledTime$clientDayTimeAccumulator += rate;
            final long wholeTicks = (long) this.decoupledTime$clientDayTimeAccumulator;
            this.decoupledTime$clientDayTimeAccumulator -= wholeTicks;
            if (wholeTicks > 0) {
                level.setDayTime(level.getDayTime() + wholeTicks);
            }
        }

        ci.cancel();
    }
}
