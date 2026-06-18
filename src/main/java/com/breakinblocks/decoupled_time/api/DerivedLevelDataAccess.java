package com.breakinblocks.decoupled_time.api;

public interface DerivedLevelDataAccess {
    long decoupledTime$getCustomDayTime();

    void decoupledTime$setCustomDayTime(long value);

    long decoupledTime$getCustomGameTime();

    void decoupledTime$setCustomGameTime(long value);

    boolean decoupledTime$isDecoupled();

    void decoupledTime$setDecoupled(boolean value);
}
