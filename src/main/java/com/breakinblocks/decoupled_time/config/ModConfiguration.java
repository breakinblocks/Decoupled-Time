package com.breakinblocks.decoupled_time.config;

import com.breakinblocks.decoupled_time.Decoupled_time;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ModConfiguration {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_INDEPENDENT_TIME;
    public static final ModConfigSpec.BooleanValue PAUSE_GAME_TIME;
    public static final ModConfigSpec.BooleanValue PAUSE_DAY_TIME;
    public static final ModConfigSpec.ConfigValue<String> DIMENSION_LIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TARGET_DIMENSIONS;
    public static final ModConfigSpec.BooleanValue SYNC_TIME_ON_JOIN;
    public static final ModConfigSpec.BooleanValue PAUSE_IN_SPECTATOR;
    public static final ModConfigSpec.DoubleValue DAY_LENGTH_MULTIPLIER;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");

        ENABLE_INDEPENDENT_TIME = BUILDER
                .comment("Master toggle for dimension time decoupling")
                .define("enableIndependentTime", true);

        PAUSE_GAME_TIME = BUILDER
                .comment("Pause gameTime progression when no active players are present.",
                        "GameTime drives scheduled ticks, entity processing, etc.")
                .define("pauseGameTime", true);

        PAUSE_DAY_TIME = BUILDER
                .comment("Pause dayTime progression when no active players are present.",
                        "DayTime controls the day/night cycle (sun/moon position).")
                .define("pauseDayTime", true);

        DIMENSION_LIST_MODE = BUILDER
                .comment("Dimension list mode. Valid values: WHITELIST, BLACKLIST")
                .define("dimensionListMode", "WHITELIST");

        TARGET_DIMENSIONS = BUILDER
                .comment("Dimensions to include/exclude based on dimensionListMode")
                .defineListAllowEmpty(
                        List.of("targetDimensions"),
                        () -> List.of("ftbteams:team_dimension"),
                        ModConfiguration::isValidResourceLocation
                );

        SYNC_TIME_ON_JOIN = BUILDER
                .comment("Immediately sync local time packet on login/dimension change")
                .define("syncTimeOnJoin", true);

        PAUSE_IN_SPECTATOR = BUILDER
                .comment("If true, spectators do not count as active players for smart pause")
                .define("pauseInSpectator", true);

        DAY_LENGTH_MULTIPLIER = BUILDER
                .comment("Day length multiplier for decoupled dimensions.",
                        "1.0 = vanilla (20 minutes per day), 2.0 = 40 minutes, 0.5 = 10 minutes, etc.",
                        "Values below 1.0 make days shorter/faster, above 1.0 make days longer/slower.",
                        "Only affects dayTime progression, not gameTime.")
                .defineInRange("dayLengthMultiplier", 1.0, 0.01, 100.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModConfiguration() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isDimensionDecoupled(final ResourceLocation dimensionId) {
        if (!ENABLE_INDEPENDENT_TIME.get()) {
            return false;
        }

        final String mode = DIMENSION_LIST_MODE.get().toUpperCase(Locale.ROOT);
        final List<? extends String> configuredDimensions = TARGET_DIMENSIONS.get();

        boolean listed = false;
        for (String configured : configuredDimensions) {
            if (matchesDimensionPattern(dimensionId, configured)) {
                listed = true;
                break;
            }
        }

        return switch (mode) {
            case "WHITELIST" -> listed;
            case "BLACKLIST" -> !listed;
            default -> {
                Decoupled_time.LOGGER.warn("Invalid dimensionListMode '{}', defaulting to WHITELIST", mode);
                yield listed;
            }
        };
    }

    private static boolean isValidResourceLocation(final Object value) {
        if (!(value instanceof String stringValue)) {
            return false;
        }

        if (!stringValue.contains("*")) {
            return ResourceLocation.tryParse(stringValue) != null;
        }

        return isValidWildcardPattern(stringValue);
    }

    private static boolean matchesDimensionPattern(final ResourceLocation dimensionId, final String configuredPattern) {
        if (!configuredPattern.contains("*")) {
            return dimensionId.toString().equals(configuredPattern);
        }

        if (!isValidWildcardPattern(configuredPattern)) {
            Decoupled_time.LOGGER.warn("Ignoring invalid wildcard dimension pattern: {}", configuredPattern);
            return false;
        }

        final String escaped = Pattern.quote(configuredPattern).replace("*", "\\E.*\\Q");
        final Pattern pattern = Pattern.compile("^" + escaped + "$");
        return pattern.matcher(dimensionId.toString()).matches();
    }

    private static boolean isValidWildcardPattern(final String value) {
        final int separatorIndex = value.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= value.length() - 1) {
            return false;
        }

        final String namespacePattern = value.substring(0, separatorIndex);
        final String pathPattern = value.substring(separatorIndex + 1);

        return isValidWildcardToken(namespacePattern, true)
                && isValidWildcardToken(pathPattern, false);
    }

    private static boolean isValidWildcardToken(final String token, final boolean namespace) {
        if (token.isEmpty()) {
            return false;
        }

        for (int index = 0; index < token.length(); index++) {
            final char character = token.charAt(index);
            if (character == '*') {
                continue;
            }

            final boolean valid = namespace
                    ? isValidNamespaceCharacter(character)
                    : isValidPathCharacter(character);
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidNamespaceCharacter(final char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= '0' && character <= '9')
                || character == '_'
                || character == '-'
                || character == '.';
    }

    private static boolean isValidPathCharacter(final char character) {
        return isValidNamespaceCharacter(character) || character == '/';
    }
}
