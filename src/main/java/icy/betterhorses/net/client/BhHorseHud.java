package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.Locale;

public final class BhHorseHud {

    private static final int STATS_TOP = 12;
    private static final int STATS_PADDING = 6;
    private static final int STATS_TEXT_COLOR = 0xFFF5F1E8;
    private static final int STATS_TITLE_COLOR = 0xFFF2C15B;
    private static final int STATS_BACKGROUND = 0xA0101010;
    private static final int STATS_ACCENT = 0xD06E5324;

    private static final double SPEED_DISPLAY = 43.2D;
    private static final double JUMP_DISPLAY = 6.0D;

    private BhHorseHud() {}

    public static void render(GuiGraphics gfx) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.screen != null) {
            return;
        }
        if (client.player.getVehicle() instanceof AbstractHorse mount) {
            BhAbilityBadges.render(gfx, client.font,
                    client.getWindow().getGuiScaledWidth(),
                    client.getWindow().getGuiScaledHeight(),
                    mount);
        }
        if (holdingUpgradedSaddle(client) && client.crosshairPickEntity instanceof AbstractHorse horse) {
            stats(gfx, client, horse);
        }
    }

    private static void stats(GuiGraphics gfx, Minecraft client, AbstractHorse horse) {
        String speedValue = String.format(Locale.ROOT, "%.1f",
                horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_DISPLAY);
        String jumpValue = String.format(Locale.ROOT, "%.1f",
                Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * JUMP_DISPLAY - 1.0D));

        IHorseData data = IHorseData.of(horse);
        ResourceKey<BreedType> breedKey = data.bh_getBreedKey();
        Component breedName = breedKey != null
                ? BreedType.displayName(breedKey, data.bh_isMixedBreed())
                : data.bh_getBreed().displayName(data.bh_isMixedBreed());
        Component title = Component.translatable("hud.icys-better-horses.horse_stats");
        Component[] lines = {
                Component.translatable("hud.icys-better-horses.gender", data.bh_getGender().displayName()),
                Component.translatable("hud.icys-better-horses.breed", breedName),
                Component.translatable("hud.icys-better-horses.speed", speedValue),
                Component.translatable("hud.icys-better-horses.jump", jumpValue),
        };

        int lineHeight = client.font.lineHeight + 2;
        int contentWidth = client.font.width(title);
        for (Component line : lines) {
            contentWidth = Math.max(contentWidth, client.font.width(line));
        }
        int boxWidth = contentWidth + STATS_PADDING * 2;
        int boxHeight = STATS_PADDING * 2 + lineHeight * (lines.length + 1);
        int left = (client.getWindow().getGuiScaledWidth() - boxWidth) / 2;

        gfx.fill(left, STATS_TOP, left + boxWidth, STATS_TOP + boxHeight, STATS_BACKGROUND);
        gfx.fill(left, STATS_TOP, left + boxWidth, STATS_TOP + 2, STATS_ACCENT);

        int textX = left + STATS_PADDING;
        int textY = STATS_TOP + STATS_PADDING;
        gfx.drawString(client.font, title, textX, textY, STATS_TITLE_COLOR, false);
        for (int i = 0; i < lines.length; i++) {
            gfx.drawString(client.font, lines[i], textX, textY + lineHeight * (i + 1),
                    STATS_TEXT_COLOR, false);
        }
    }

    private static boolean holdingUpgradedSaddle(Minecraft client) {
        return client.player != null
                && (client.player.getMainHandItem().is(ModItems.UPGRADED_SADDLE.get())
                || client.player.getOffhandItem().is(ModItems.UPGRADED_SADDLE.get()));
    }
}
