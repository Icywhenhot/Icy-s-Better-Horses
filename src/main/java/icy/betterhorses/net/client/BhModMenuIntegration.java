package icy.betterhorses.net.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import icy.betterhorses.net.BhConfig;
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

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.icys-better-horses.category.general"));
        boolean[] values = {
                BhConfig.stabilizerEnabled(),
                BhConfig.medkitEnabled(),
                BhConfig.hitchpostEnabled(),
                BhConfig.hoovesEnabled(),
                BhConfig.horseExclusivityEnabled(),
                BhConfig.multiRidingEnabled(),
                BhConfig.horseCombatEnabled(),
                BhConfig.brickBreakEnabled(),
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
        general.addEntry(bh_toggle(eb, "brick_break", values, 7));
        general.addEntry(bh_toggle(eb, "transparent_horses", values, 8));
        general.addEntry(bh_toggle(eb, "gender_breeding", values, 9));

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

        builder.setSavingRunnable(() -> {
            BhConfig.apply(values[0], values[1], values[2], values[3], values[4], values[5],
                    values[6], values[7], values[8], values[9]);
            KeyMapping.resetMapping();
            Minecraft.getInstance().options.save();
        });
        return builder.build();
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
