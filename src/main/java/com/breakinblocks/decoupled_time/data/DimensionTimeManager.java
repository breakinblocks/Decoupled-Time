package com.breakinblocks.decoupled_time.data;

import com.breakinblocks.decoupled_time.api.DerivedLevelDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DerivedLevelData;

public final class DimensionTimeManager {
    private static final SavedData.Factory<DimensionTimeSavedData> FACTORY = new SavedData.Factory<>(
            DimensionTimeSavedData::create,
            DimensionTimeSavedData::load
    );

    private DimensionTimeManager() {
        throw new IllegalStateException("Utility class");
    }

    public static DimensionTimeSavedData getOrCreate(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DimensionTimeSavedData.DATA_ID);
    }

    public static void initializeFromStorage(final ServerLevel level) {
        if (!(level.getLevelData() instanceof DerivedLevelData derivedLevelData)) {
            return;
        }

        final DerivedLevelDataAccess accessor = (DerivedLevelDataAccess) derivedLevelData;
        final DimensionTimeSavedData data = getOrCreate(level);

        accessor.decoupledTime$setDecoupled(true);
        accessor.decoupledTime$setCustomDayTime(data.getCustomDayTime());
        accessor.decoupledTime$setCustomGameTime(data.getCustomGameTime());
    }

    public static void flushToStorage(final ServerLevel level) {
        if (!(level.getLevelData() instanceof DerivedLevelData derivedLevelData)) {
            return;
        }

        final DerivedLevelDataAccess accessor = (DerivedLevelDataAccess) derivedLevelData;
        final DimensionTimeSavedData data = getOrCreate(level);

        data.updateTime(accessor.decoupledTime$getCustomDayTime(), accessor.decoupledTime$getCustomGameTime());
    }
}
