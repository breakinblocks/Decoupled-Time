package com.breakinblocks.decoupled_time;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import com.breakinblocks.decoupled_time.config.ModConfiguration;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(Decoupled_time.MODID)
public class Decoupled_time {
    public static final String MODID = "decoupled_time";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Decoupled_time(final IEventBus modEventBus, final ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ModConfiguration.SPEC);
    }
}
