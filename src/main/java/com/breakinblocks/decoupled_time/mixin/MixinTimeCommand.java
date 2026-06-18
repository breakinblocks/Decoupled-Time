package com.breakinblocks.decoupled_time.mixin;

import com.breakinblocks.decoupled_time.commands.TimeCommandDimensionSupport;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(TimeCommand.class)
public abstract class MixinTimeCommand {
    @Inject(method = "register", at = @At("TAIL"))
    private static void decoupledTime$registerDimensionTargetedTimeCommands(
            final CommandDispatcher<CommandSourceStack> dispatcher,
            final CallbackInfo ci
    ) {
        dispatcher.register(
                Commands.literal("time")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("time", TimeArgument.time())
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.setTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        context.getArgument("time", Integer.class)
                                                ))))
                                .then(Commands.literal("day")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.setTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        1000
                                                ))))
                                .then(Commands.literal("noon")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.setTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        6000
                                                ))))
                                .then(Commands.literal("night")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.setTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        13000
                                                ))))
                                .then(Commands.literal("midnight")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.setTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        18000
                                                )))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("time", TimeArgument.time())
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.addTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"),
                                                        context.getArgument("time", Integer.class)
                                                )))))
                        .then(Commands.literal("query")
                                .then(Commands.literal("daytime")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.queryDayTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension")
                                                ))))
                                .then(Commands.literal("gametime")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.queryGameTimeInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension")
                                                ))))
                                .then(Commands.literal("day")
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .suggests(MixinTimeCommand::decoupledTime$suggestAvailableDimensions)
                                                .executes(context -> TimeCommandDimensionSupport.queryDayInDimension(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension")
                                                )))))
        );
    }

    private static CompletableFuture<Suggestions> decoupledTime$suggestAvailableDimensions(
            final CommandContext<CommandSourceStack> context,
            final SuggestionsBuilder builder
    ) {
        final CommandSourceStack source = context.getSource();
        return SharedSuggestionProvider.suggestResource(source.levels().stream().map(dimension -> dimension.location()), builder);
    }

    @Inject(method = "setTime", at = @At("HEAD"), cancellable = true)
    private static void decoupledTime$setLocalTime(
            final CommandSourceStack source,
            final int time,
            final CallbackInfoReturnable<Integer> cir
    ) {
        cir.setReturnValue(TimeCommandDimensionSupport.setTimeInDimension(source, source.getLevel(), time));
    }

    @Inject(method = "addTime", at = @At("HEAD"), cancellable = true)
    private static void decoupledTime$addLocalTime(
            final CommandSourceStack source,
            final int amount,
            final CallbackInfoReturnable<Integer> cir
    ) {
        cir.setReturnValue(TimeCommandDimensionSupport.addTimeInDimension(source, source.getLevel(), amount));
    }
}
