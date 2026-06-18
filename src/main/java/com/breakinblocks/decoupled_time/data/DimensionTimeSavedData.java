package com.breakinblocks.decoupled_time.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public final class DimensionTimeSavedData extends SavedData {
    public static final String DATA_ID = "decoupled_time_dimension_data";
    private static final String KEY_CUSTOM_DAY_TIME = "CustomDayTime";
    private static final String KEY_CUSTOM_GAME_TIME = "CustomGameTime";

    private long customDayTime;
    private long customGameTime;

    public DimensionTimeSavedData(final long customDayTime, final long customGameTime) {
        this.customDayTime = customDayTime;
        this.customGameTime = customGameTime;
    }

    public static DimensionTimeSavedData create() {
        return new DimensionTimeSavedData(0L, 0L);
    }

    public static DimensionTimeSavedData load(final CompoundTag tag, final HolderLookup.Provider lookupProvider) {
        return new DimensionTimeSavedData(tag.getLong(KEY_CUSTOM_DAY_TIME), tag.getLong(KEY_CUSTOM_GAME_TIME));
    }

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        tag.putLong(KEY_CUSTOM_DAY_TIME, this.customDayTime);
        tag.putLong(KEY_CUSTOM_GAME_TIME, this.customGameTime);
        return tag;
    }

    public long getCustomDayTime() {
        return this.customDayTime;
    }

    public long getCustomGameTime() {
        return this.customGameTime;
    }

    public void updateTime(final long dayTime, final long gameTime) {
        this.customDayTime = dayTime;
        this.customGameTime = gameTime;
        this.setDirty();
    }
}
