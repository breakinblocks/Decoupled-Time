package com.breakinblocks.decoupled_time.mixin;

import com.breakinblocks.decoupled_time.api.DerivedLevelDataAccess;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DerivedLevelData.class)
public abstract class MixinDerivedLevelData implements DerivedLevelDataAccess {
    @Unique
    private long decoupledTime$customDayTime;

    @Unique
    private long decoupledTime$customGameTime;

    @Unique
    private boolean decoupledTime$isDecoupled;

    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onGetDayTime(final CallbackInfoReturnable<Long> cir) {
        if (this.decoupledTime$isDecoupled) {
            cir.setReturnValue(this.decoupledTime$customDayTime);
        }
    }

    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onSetDayTime(final long time, final CallbackInfo ci) {
        if (this.decoupledTime$isDecoupled) {
            this.decoupledTime$customDayTime = time;
            ci.cancel();
        }
    }

    @Inject(method = "getGameTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onGetGameTime(final CallbackInfoReturnable<Long> cir) {
        if (this.decoupledTime$isDecoupled) {
            cir.setReturnValue(this.decoupledTime$customGameTime);
        }
    }

    @Inject(method = "setGameTime", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$onSetGameTime(final long time, final CallbackInfo ci) {
        if (this.decoupledTime$isDecoupled) {
            this.decoupledTime$customGameTime = time;
            ci.cancel();
        }
    }

    @Override
    public long decoupledTime$getCustomDayTime() {
        return this.decoupledTime$customDayTime;
    }

    @Override
    public void decoupledTime$setCustomDayTime(final long value) {
        this.decoupledTime$customDayTime = value;
    }

    @Override
    public long decoupledTime$getCustomGameTime() {
        return this.decoupledTime$customGameTime;
    }

    @Override
    public void decoupledTime$setCustomGameTime(final long value) {
        this.decoupledTime$customGameTime = value;
    }

    @Override
    public boolean decoupledTime$isDecoupled() {
        return this.decoupledTime$isDecoupled;
    }

    @Override
    public void decoupledTime$setDecoupled(final boolean value) {
        this.decoupledTime$isDecoupled = value;
    }
}
