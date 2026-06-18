package com.breakinblocks.decoupled_time.compat.incontrol;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin that conditionally loads InControl compatibility mixins
 * only when InControl is present in the mod-list.
 *
 * Referenced by {@code decoupled_time.incontrol.mixins.json} via the
 * {@code "plugin"} key.
 */
public class InControlMixinPlugin implements IMixinConfigPlugin {

    private static final String INCONTROL_MOD_ID = "incontrol";
    private static final boolean INCONTROL_LOADED;

    static {
        INCONTROL_LOADED = LoadingModList.get().getModFileById(INCONTROL_MOD_ID) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {
        // no-op
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return INCONTROL_LOADED;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // no-op
    }

    /**
     * Dynamically injects the InControl compat mixins only when the mod is
     * present. This avoids class-not-found crashes when InControl is absent.
     */
    @Override
    public List<String> getMixins() {
        if (INCONTROL_LOADED) {
            return List.of("MixinInControlDataStorage");
        }
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
}
