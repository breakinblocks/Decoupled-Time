package com.breakinblocks.decoupled_time.compat.incontrol.mixin;

import com.breakinblocks.decoupled_time.api.DecoupledTimeAPI;
import mcjty.incontrol.rules.PhaseRule;
import mcjty.incontrol.rules.RulesManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redirects InControl's global day-counter and phase system to return
 * per-dimension values for dimensions that are decoupled by our mod.
 *
 * <h3>Context capture pattern</h3>
 * InControl's spawn-rule lambdas always call {@code DataStorage.getData(world)}
 * before calling instance methods like {@code getDaycounter()} or
 * {@code getPhases()}. {@code getData()} receives the <em>actual</em>
 * dimension (via {@code query.getWorld(event)}) but internally always fetches
 * the Overworld's data storage. We capture the dimension in a
 * {@link ThreadLocal} during {@code getData()} and read it back in the
 * instance methods that follow.
 *
 * <h3>Daycount redirect</h3>
 * For decoupled dimensions, {@code getDaycounter()} returns
 * {@code (int)(dayTime / 24000)} from the dimension's own time instead of the
 * global Overworld counter.
 *
 * <h3>Phase redirect</h3>
 * Phase rules (from {@code phases.json}) are normally evaluated only against
 * the Overworld every 10 ticks. We piggyback on that evaluation cycle to also
 * evaluate phase rules for every loaded decoupled dimension, storing results in
 * a per-dimension map. {@code getPhases()} then returns the correct set for
 * whichever dimension is in context.
 * <p>
 * Because {@code RulesManager} caches filtered rule lists globally (not
 * per-dimension), we track which dimension's phases were used to build the
 * cache and invalidate it when the querying dimension changes.
 *
 * <h3>Conditional loading</h3>
 * This mixin is registered dynamically by
 * {@link com.breakinblocks.decoupled_time.compat.incontrol.InControlMixinPlugin}
 * and will never be loaded when InControl is absent.
 */
@Mixin(targets = "mcjty.incontrol.data.DataStorage")
public abstract class MixinInControlDataStorage {

    // ── Context capture ─────────────────────────────────────────────────

    /**
     * Holds the dimension that was last passed to {@code getData()}.
     * Consumed (not cleared) by {@code getDaycounter()} and
     * {@code getPhases()}.
     */
    @Unique
    private static final ThreadLocal<LevelAccessor> decoupledTime$contextLevel = new ThreadLocal<>();

    // ── Per-dimension phase storage ─────────────────────────────────────

    /**
     * Evaluated phase names for each loaded decoupled dimension.
     * Populated in the {@code tickPhases} injection; queried in the
     * {@code getPhases} injection.
     */
    @Unique
    private final Map<ResourceKey<Level>, Set<String>> decoupledTime$dimensionPhases = new HashMap<>();

    /**
     * Tracks which dimension's phases are currently reflected in
     * {@link RulesManager}'s cached filtered rule lists. When the querying
     * dimension changes we call {@link RulesManager#onPhaseChange()} to
     * force a cache rebuild with the correct phase set.
     * <p>
     * {@code null} means the cache holds Overworld / non-decoupled phases.
     */
    @Unique
    private static ResourceKey<Level> decoupledTime$lastPhaseCacheDim = null;

    // ── getData: capture context ────────────────────────────────────────

    /**
     * Captures the requesting dimension before {@code getData()} fetches
     * the Overworld's data storage.
     */
    @Inject(method = "getData", at = @At("HEAD"))
    private static void decoupledTime$captureContext(LevelAccessor world,
                                                     CallbackInfoReturnable<?> cir) {
        decoupledTime$contextLevel.set(world);
    }

    // ── getDaycounter: per-dimension redirect ───────────────────────────

    /**
     * If the captured context level belongs to a decoupled dimension,
     * return the day-count derived from that dimension's own
     * {@code dayTime} instead of the global Overworld counter.
     *
     * <p>Formula: {@code (int)(dayTime / 24000)}. This matches
     * InControl's own dawn-transition counter: day 0 for ticks 0-23999,
     * day 1 for 24000-47999, etc.</p>
     */
    @Inject(method = "getDaycounter", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$redirectDaycounter(CallbackInfoReturnable<Integer> cir) {
        LevelAccessor world = decoupledTime$contextLevel.get();
        if (world instanceof Level level && DecoupledTimeAPI.isDimensionDecoupled(level)) {
            cir.setReturnValue(DecoupledTimeAPI.getDimensionDayCount(level));
        }
    }

    // ── getPhases: per-dimension redirect ───────────────────────────────

    /**
     * Returns the per-dimension phase set for decoupled dimensions, and
     * invalidates the {@link RulesManager} rule cache when the querying
     * dimension changes.
     */
    @Inject(method = "getPhases", at = @At("HEAD"), cancellable = true)
    private void decoupledTime$redirectGetPhases(CallbackInfoReturnable<Set<String>> cir) {
        LevelAccessor world = decoupledTime$contextLevel.get();
        if (world instanceof Level level && DecoupledTimeAPI.isDimensionDecoupled(level)) {
            ResourceKey<Level> dimKey = level.dimension();

            // Invalidate RulesManager's cached rule lists when switching dimensions
            if (!dimKey.equals(decoupledTime$lastPhaseCacheDim)) {
                RulesManager.onPhaseChange();
                decoupledTime$lastPhaseCacheDim = dimKey;
            }

            Set<String> dimPhases = decoupledTime$dimensionPhases.get(dimKey);
            cir.setReturnValue(dimPhases != null ? Collections.unmodifiableSet(dimPhases) : Set.of());
        } else {
            // Non-decoupled: invalidate if the cache was built for a decoupled dim
            if (decoupledTime$lastPhaseCacheDim != null) {
                RulesManager.onPhaseChange();
                decoupledTime$lastPhaseCacheDim = null;
            }
        }
    }

    // ── tickPhases: evaluate for all decoupled dimensions ───────────────

    /**
     * After InControl finishes its normal Overworld phase evaluation, we
     * run the same logic for every loaded decoupled dimension. This
     * piggybacks on the existing 10-tick cadence (the {@code checkCounter}
     * guard in {@code tick()}).
     *
     * <p>Phase rules that check {@code time}, {@code daycount}, etc. will
     * naturally pick up per-dimension values because
     * {@code PhaseRule.match(world)} calls through to
     * {@code GenericRuleEvaluator.match()}, which calls
     * {@code query.getWorld(event)} (the world we pass), and
     * {@code DataStorage.getData(world)} (which sets our context
     * ThreadLocal).</p>
     */
    @Inject(method = "tickPhases", at = @At("TAIL"))
    private void decoupledTime$tickDimensionPhases(Level world, CallbackInfo ci) {
        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }

        boolean anyDirty = false;

        for (ServerLevel level : server.getAllLevels()) {
            ResourceKey<Level> dimKey = level.dimension();
            if (dimKey.equals(Level.OVERWORLD)) {
                continue;
            }
            if (!DecoupledTimeAPI.isDimensionDecoupled(level)) {
                // Clean up if a previously-decoupled dimension was removed from config
                decoupledTime$dimensionPhases.remove(dimKey);
                continue;
            }

            Set<String> dimPhases = decoupledTime$dimensionPhases
                    .computeIfAbsent(dimKey, k -> new HashSet<>());

            boolean dirty = false;
            for (PhaseRule rule : RulesManager.phaseRules) {
                if (rule.match(level)) {
                    dirty |= dimPhases.add(rule.getName());
                } else {
                    dirty |= dimPhases.remove(rule.getName());
                }
            }
            anyDirty |= dirty;
        }

        if (anyDirty) {
            // Force cached rule lists to be rebuilt on next query
            RulesManager.onPhaseChange();
        }
    }
}
