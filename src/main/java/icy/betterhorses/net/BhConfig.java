package icy.betterhorses.net;

import net.neoforged.neoforge.common.ModConfigSpec;

// In-game config backed by NeoForge's ModConfigSpec (config/icys_better_horses-common.toml). NeoForge auto-renders the editor screen from the Mods list. Accessors keep their old names so the rest of the mod is unchanged.
public final class BhConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue STABILIZER =
            toggle("stabilizer", "Enable the horse stabilizer gear (gliding descent and safe landings).");
    private static final ModConfigSpec.BooleanValue MEDKIT =
            toggle("medkit", "Enable the horse medkit gear (auto-heals the horse when badly hurt).");
    private static final ModConfigSpec.BooleanValue HITCHPOST =
            toggle("hitchpost", "Enable tethering horses to hitchposts.");
    private static final ModConfigSpec.BooleanValue HOOVES =
            toggle("hooves", "Enable the horse hooves gear (reduced fall damage and frost/snow walking).");
    private static final ModConfigSpec.BooleanValue HORSE_EXCLUSIVITY =
            toggle("horse_exclusivity", "Only the owner may ride or open the inventory of an owned horse.");
    private static final ModConfigSpec.BooleanValue MULTI_RIDING =
            toggle("multiriding", "Allow a second player to ride behind the owner.");

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BhConfig() {}

    private static ModConfigSpec.BooleanValue toggle(String key, String comment) {
        return BUILDER
                .translation("config.icys_better_horses." + key)
                .comment(comment)
                .define(key, true);
    }

    public static boolean stabilizerEnabled() {
        return STABILIZER.getAsBoolean();
    }

    public static boolean medkitEnabled() {
        return MEDKIT.getAsBoolean();
    }

    public static boolean hitchpostEnabled() {
        return HITCHPOST.getAsBoolean();
    }

    public static boolean hoovesEnabled() {
        return HOOVES.getAsBoolean();
    }

    public static boolean horseExclusivityEnabled() {
        return HORSE_EXCLUSIVITY.getAsBoolean();
    }

    public static boolean multiRidingEnabled() {
        return MULTI_RIDING.getAsBoolean();
    }
}
