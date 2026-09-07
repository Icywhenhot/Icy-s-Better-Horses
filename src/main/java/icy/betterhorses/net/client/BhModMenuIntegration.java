package icy.betterhorses.net.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IcysBetterHorsesClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BhModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return parent -> null;
        }
        return BhModMenuIntegration::buildScreen;
    }

    private static Screen buildScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.icys-better-horses.title"));

        ConfigEntryBuilder eb = builder.entryBuilder();
        boolean locked = BhConfig.serverManaged();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.general"));
        if (locked) {
            general.addEntry(eb.startTextDescription(
                    Component.translatable("config.icys-better-horses.server_managed")).build());
        }
        boolean[] values = {
                BhConfig.stabilizerEnabled(),
                BhConfig.medkitEnabled(),
                BhConfig.hitchpostEnabled(),
                BhConfig.hoovesEnabled(),
                BhConfig.horseExclusivityEnabled(),
                BhConfig.multiRidingEnabled(),
                BhConfig.horseCombatEnabled(),
                BhConfig.transparentHorsesEnabled(),
                BhConfig.genderBreedingEnabled(),
        };
        general.addEntry(bh_toggle(eb, "stabilizer", values, 0));
        general.addEntry(bh_toggle(eb, "medkit", values, 1));
        general.addEntry(bh_toggle(eb, "hitchpost", values, 2));
        general.addEntry(bh_toggle(eb, "hooves", values, 3));
        general.addEntry(bh_toggle(eb, "horse_exclusivity", values, 4));
        general.addEntry(bh_toggle(eb, "multiriding", values, 5));
        general.addEntry(bh_toggle(eb, "horse_combat", values, 6));
        general.addEntry(bh_toggle(eb, "transparent_horses", values, 7));
        general.addEntry(bh_toggle(eb, "gender_breeding", values, 8));

        boolean[] masters = {BhConfig.classAbilitiesEnabled(), BhConfig.breedAbilitiesEnabled()};
        Map<BhAbility, Boolean> picks = new EnumMap<>(BhConfig.abilities());

        ConfigCategory classes = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.class_abilities"));
        classes.addEntry(bh_master(eb, "class_abilities", masters, 0));
        for (BreedArchetype arch : BreedArchetype.values()) {
            List<AbstractConfigListEntry> rows = new ArrayList<>();
            for (BhAbility ability : BhAbility.values()) {
                if (ability.archetype() == arch) {
                    rows.add(bh_ability(eb, ability, picks));
                }
            }
            if (!rows.isEmpty()) {
                classes.addEntry(eb.startSubCategory(Component.translatable(
                        "config.icys-better-horses.class." + arch.name().toLowerCase(Locale.ROOT)), rows)
                        .setExpanded(true)
                        .build());
            }
        }

        ConfigCategory breeds = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.breed_abilities"));
        breeds.addEntry(bh_master(eb, "breed_abilities", masters, 1));
        for (HorseBreed breed : HorseBreed.values()) {
            List<AbstractConfigListEntry> rows = new ArrayList<>();
            for (BhAbility ability : BhAbility.values()) {
                if (ability.breed() == breed) {
                    rows.add(bh_ability(eb, ability, picks));
                }
            }
            if (rows.isEmpty()) {
                continue;
            }
            breeds.addEntry(eb.startSubCategory(Component.translatable(
                    "book.icys-better-horses.stable_handbook.classes.breed_"
                            + breed.name().toLowerCase(Locale.ROOT) + ".name"), rows)
                    .setExpanded(false)
                    .build());
        }

        ConfigCategory keybinds = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.keybinds"));
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.call_key"), IcysBetterHorsesClient.CALL_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.call_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.radial_key"), IcysBetterHorsesClient.RADIAL_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.radial_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.manage_key"), IcysBetterHorsesClient.MANAGE_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.manage_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.gear_key"), IcysBetterHorsesClient.GEAR_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.gear_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.rear_key"), IcysBetterHorsesClient.REAR_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.rear_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.free_look_key"),
                        IcysBetterHorsesClient.FREE_LOOK_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.free_look_key.tooltip"))
                .build());
        keybinds.addEntry(eb.fillKeybindingField(
                        Component.translatable("config.icys-better-horses.cart_size_key"),
                        IcysBetterHorsesClient.CART_SIZE_KEY)
                .setTooltip(Component.translatable("config.icys-better-horses.cart_size_key.tooltip"))
                .build());

        builder.setSavingRunnable(() -> {
            BhConfig.apply(values[0], values[1], values[2], values[3], values[4], values[5],
                    values[6], values[7], values[8]);
            BhConfig.applyAbilities(masters[0], masters[1], picks);
            KeyMapping.resetMapping();
            Minecraft.getInstance().options.save();
        });
        return builder.build();
    }

    private static AbstractConfigListEntry<Boolean> bh_master(
            ConfigEntryBuilder eb, String key, boolean[] flags, int index) {
        return eb.startBooleanToggle(Component.translatable("config.icys-better-horses." + key), flags[index])
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.icys-better-horses." + key + ".tooltip"))
                .setSaveConsumer(value -> flags[index] = value)
                .build();
    }

    private static AbstractConfigListEntry<Boolean> bh_ability(
            ConfigEntryBuilder eb, BhAbility ability, Map<BhAbility, Boolean> picks) {
        String base = "config.icys-better-horses.ability." + ability.key();
        return eb.startBooleanToggle(Component.translatable(base), picks.getOrDefault(ability, true))
                .setDefaultValue(true)
                .setTooltip(Component.translatable(base + ".desc"))
                .setSaveConsumer(value -> picks.put(ability, value))
                .build();
    }

    private static AbstractConfigListEntry<Boolean> bh_toggle(
            ConfigEntryBuilder eb, String key, boolean[] values, int index) {
        return eb.startBooleanToggle(Component.translatable("config.icys-better-horses." + key), values[index])
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.icys-better-horses." + key + ".tooltip"))
                .setSaveConsumer(value -> values[index] = value)
                .build();
    }
}
