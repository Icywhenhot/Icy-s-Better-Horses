package icy.betterhorses.net.client;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class BhAbilityBadges {

    private static final int INK = 0xFF3B200B;
    private static final int INK_SOFT = 0xFF6B4A2A;
    private static final int FILL = 0xFFBF360C;
    private static final int FILL_COOL = 0xFFAD8865;

    private static final int HEIGHT = 30;
    private static final int MARGIN = 6;
    private static final int GAP = 2;
    private static final float TAU = 0.09F;

    private static final int ICON = 16;
    private static final int ICON_Y = 7;
    private static final int TEXT_Y = 7;
    private static final int TEXT_W = 70;
    private static final float TEXT_SCALE = 0.75F;
    private static final int BAR_X = 28;
    private static final int BAR_W = 66;
    private static final int VALUE_Y = 18;

    private static final Identifier BAR = Identifier.fromNamespaceAndPath(
            "icys-better-horses", "textures/gui/hud/badge_fill.png");

    private static final Plate WIDE = new Plate(Identifier.fromNamespaceAndPath(
            "icys-better-horses", "textures/gui/hud/badge_plate.png"), 102, 7, 27);
    private static final Plate NARROW = new Plate(Identifier.fromNamespaceAndPath(
            "icys-better-horses", "textures/gui/hud/badge_plate_no_bar.png"), 70, 8, 28);
    private static final Map<String, Boolean> found = new HashMap<>();

    private static final String ABILITY_SLOT = "ability";
    private static final String ROAD_SLOT = "road";

    private static final int BASH_SIZE = 16;
    private static final int BASH_FRAMES = 10;
    private static final int HOTBAR_HALF = 91;
    private static final int HOTBAR_GAP = 4;
    private static final int OFFHAND_WIDTH = 29;
    private static final int HOTBAR_HEIGHT = 22;

    private static final Identifier[] BASH = new Identifier[BASH_FRAMES + 1];

    static {
        for (int i = 0; i <= BASH_FRAMES; i++) {
            BASH[i] = Identifier.fromNamespaceAndPath(
                    "icys-better-horses", "textures/gui/hud/bash_" + i + ".png");
        }
    }

    private static final BhAnim.Lift lift = new BhAnim.Lift();
    private static final Map<String, Badge> shown = new LinkedHashMap<>();

    private BhAbilityBadges() {}

    public static void reset() {
        shown.clear();
        found.clear();
    }

    public static void render(GuiGraphicsExtractor gfx, Font font, int screenW, int screenH,
                              AbstractHorse horse) {
        IHorseData data = IHorseData.of(horse);
        HorseBreed breed = data.bh_getBreed();
        if (!breed.isRealBreed()) {
            return;
        }

        Badge ability = read(breed, data.bh_getSurge(), false);
        Badge road = read(breed, data.bh_getPerkSurge(), true);
        if (ability != null) {
            shown.put(ABILITY_SLOT, ability);
        }
        if (road != null) {
            shown.put(ROAD_SLOT, road);
        }

        lift.beginFrame(TAU);
        int y = screenH / 2 - 30;
        Iterator<Map.Entry<String, Badge>> it = shown.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Badge> entry = it.next();
            boolean live = entry.getKey().equals(ABILITY_SLOT) ? ability != null : road != null;
            float in = lift.get(entry.getKey(), live, 1.0F);
            if (!live && in <= 0.02F) {
                it.remove();
                continue;
            }
            Badge badge = entry.getValue();
            Plate plate = badge.fill < 0.0F ? NARROW : WIDE;
            draw(gfx, font, screenW - plate.width() - MARGIN, y, badge, plate, in);
            y += HEIGHT + GAP;
        }

        shield(gfx, screenW, screenH, data.bh_getCharge());
    }

    private static @Nullable Badge read(HorseBreed breed, int packed, boolean road) {
        int phase = BhSurge.phase(packed);
        if (phase == BhSurge.IDLE) {
            return null;
        }
        String key = road ? "road" : breed.name().toLowerCase(Locale.ROOT);
        Component label = Component.translatable("hud.icys-better-horses.ability." + key);
        int percent = BhSurge.percent(packed);
        Component value = switch (phase) {
            case BhSurge.COOLING -> Component.translatable("hud.icys-better-horses.ability.cooling");
            case BhSurge.ARMED -> Component.translatable("hud.icys-better-horses.ability.ready");
            default -> percent > 0
                    ? Component.translatable("hud.icys-better-horses.ability.bonus", percent)
                    : Component.empty();
        };
        return new Badge(key, label, value, BhSurge.span(packed) > 0 ? BhSurge.fill(packed) : -1.0F,
                phase == BhSurge.COOLING);
    }

    private static void draw(GuiGraphicsExtractor gfx, Font font, int x, int y,
                             Badge badge, Plate plate, float in) {
        float a = BhAnim.clamp01(in);
        if (a <= 0.01F) {
            return;
        }
        int slide = Math.round((1.0F - BhAnim.easeOutCubic(a)) * 14.0F);
        int left = x + slide;
        int tint = (Math.round(255.0F * a) << 24) | 0xFFFFFF;

        gfx.blit(RenderPipelines.GUI_TEXTURED, plate.tex(), left, y,
                0.0F, 0.0F, plate.width(), HEIGHT, plate.width(), HEIGHT, tint);

        if (icon(badge.key)) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, iconId(badge.key),
                    left + plate.iconX(), y + ICON_Y, 0.0F, 0.0F, ICON, ICON, ICON, ICON, tint);
        }

        small(gfx, font, badge.label, left + plate.textX(), y + TEXT_Y, BhAnim.fade(INK, a));

        int valueWidth = scaled(font.width(badge.value));
        if (valueWidth <= 0) {
            return;
        }
        if (badge.fill < 0.0F) {
            small(gfx, font, badge.value, left + plate.textX(), y + VALUE_Y,
                    BhAnim.fade(INK_SOFT, a));
            return;
        }
        if (scaled(font.width(badge.label)) + valueWidth + 3 <= TEXT_W) {
            small(gfx, font, badge.value, left + plate.textX() + TEXT_W - valueWidth, y + TEXT_Y,
                    BhAnim.fade(INK_SOFT, a));
        }

        int span = Math.round(BAR_W * badge.fill);
        if (span > 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, BAR, left + BAR_X, y,
                    BAR_X, 0.0F, span, HEIGHT, WIDE.width(), HEIGHT,
                    BhAnim.fade(badge.cooling ? FILL_COOL : FILL, a));
        }
    }

    private static int scaled(int width) {
        return Math.round(width * TEXT_SCALE);
    }

    private static void small(GuiGraphicsExtractor gfx, Font font, Component text,
                              int x, int y, int color) {
        Matrix3x2fStack pose = gfx.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(TEXT_SCALE, TEXT_SCALE);
        gfx.text(font, text, 0, 0, color, false);
        pose.popMatrix();
    }

    public static Identifier chargeIcon(int percent) {
        return BASH[Math.clamp(Math.round(percent * BASH_FRAMES / 100.0F), 0, BASH_FRAMES)];
    }

    private static Identifier iconId(String key) {
        return Identifier.fromNamespaceAndPath(
                "icys-better-horses", "textures/gui/hud/icon_" + key + ".png");
    }

    private static boolean icon(String key) {
        return present(iconId(key));
    }

    private static boolean present(Identifier id) {
        return found.computeIfAbsent(id.toString(), k -> Minecraft.getInstance()
                .getResourceManager().getResource(id).isPresent());
    }

    private static void shield(GuiGraphicsExtractor gfx, int screenW, int screenH, int charge) {
        if (charge < 0) {
            return;
        }
        boolean lefty = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT;
        int x = screenW / 2 + HOTBAR_HALF + HOTBAR_GAP + (lefty ? OFFHAND_WIDTH : 0);
        int y = screenH - HOTBAR_HEIGHT + (HOTBAR_HEIGHT - BASH_SIZE) / 2;
        int frame = Math.clamp(Math.round(charge * BASH_FRAMES / 100.0F), 0, BASH_FRAMES);

        gfx.blit(RenderPipelines.GUI_TEXTURED, BASH[frame], x, y,
                0.0F, 0.0F, BASH_SIZE, BASH_SIZE, BASH_SIZE, BASH_SIZE);
    }

    private record Badge(String key, Component label, Component value, float fill, boolean cooling) {}

    private record Plate(Identifier tex, int width, int iconX, int textX) {}

    static {
        BhClientCaches.register(BhAbilityBadges::reset);
    }
}
