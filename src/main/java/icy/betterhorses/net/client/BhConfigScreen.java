package icy.betterhorses.net.client;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.BhTuning;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IcysBetterHorsesClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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

public final class BhConfigScreen {

    public static Screen create(Screen parent) {
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
        Map<BhFeature, Boolean> picks = new EnumMap<>(BhConfig.featureView());
        for (BhFeature feature : BhFeature.values()) {
            general.addEntry(bh_toggle(eb, feature, picks));
        }

        BhTuning start = BhConfig.tuningView();
        int[] nums = {start.bondAmount(), start.bondMinutes(), start.spawnWeight(),
                start.groupMin(), start.groupMax()};
        double[] floor = {start.spawnFloor()};

        ConfigCategory tuning = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.tuning"));
        if (locked) {
            tuning.addEntry(eb.startTextDescription(
                    Component.translatable("config.icys-better-horses.server_managed")).build());
        }
        tuning.addEntry(bh_number(eb, "bond_per_interval", nums, 0, 0, 100));
        tuning.addEntry(bh_number(eb, "bond_interval_minutes", nums, 1, 1, 1440));
        tuning.addEntry(bh_number(eb, "spawn_weight", nums, 2, 0, 1000));
        tuning.addEntry(bh_number(eb, "spawn_group_min", nums, 3, 1, 32));
        tuning.addEntry(bh_number(eb, "spawn_group_max", nums, 4, 1, 32));
        tuning.addEntry(eb.startDoubleField(
                        Component.translatable("config.icys-better-horses.spawn_probability_floor"), floor[0])
                .setMin(0.0D).setMax(1.0D)
                .setDefaultValue(BhTuning.defaults().spawnFloor())
                .setTooltip(Component.translatable("config.icys-better-horses.spawn_probability_floor.tooltip"))
                .setSaveConsumer(value -> floor[0] = value)
                .build());

        boolean[] masters = {BhConfig.classAbilitiesEnabled(), BhConfig.breedAbilitiesEnabled()};
        Map<BhAbility, Boolean> abilityPicks = new EnumMap<>(BhConfig.abilities());

        ConfigCategory classes = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.class_abilities"));
        classes.addEntry(bh_master(eb, "class_abilities", masters, 0));
        for (BreedArchetype arch : BreedArchetype.values()) {
            List<AbstractConfigListEntry> rows = new ArrayList<>();
            for (BhAbility ability : BhAbility.values()) {
                if (ability.archetype() == arch) {
                    rows.add(bh_ability(eb, ability, abilityPicks));
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
                    rows.add(bh_ability(eb, ability, abilityPicks));
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
            BhConfig.apply(picks, new BhTuning(nums[0], nums[1], nums[2], nums[3], nums[4], floor[0]));
            BhConfig.applyAbilities(masters[0], masters[1], abilityPicks);
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
            ConfigEntryBuilder eb, BhFeature feature, Map<BhFeature, Boolean> picks) {
        String base = "config.icys-better-horses." + feature.key();
        return eb.startBooleanToggle(Component.translatable(base), picks.getOrDefault(feature, true))
                .setDefaultValue(true)
                .setTooltip(Component.translatable(base + ".tooltip"))
                .setSaveConsumer(value -> picks.put(feature, value))
                .build();
    }

    private static AbstractConfigListEntry<Integer> bh_number(
            ConfigEntryBuilder eb, String key, int[] values, int index, int min, int max) {
        String base = "config.icys-better-horses." + key;
        return eb.startIntField(Component.translatable(base), values[index])
                .setMin(min)
                .setMax(max)
                .setTooltip(Component.translatable(base + ".tooltip"))
                .setSaveConsumer(value -> values[index] = value)
                .build();
    }
}
