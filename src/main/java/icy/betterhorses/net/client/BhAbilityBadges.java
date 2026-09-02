package icy.betterhorses.net.client;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class BhAbilityBadges {

    private static final int BG = 0xA0101010;
    private static final int STRIPE = 0xD06E5324;
    private static final int TITLE = 0xFFF2C15B;
    private static final int TEXT = 0xFFF5F1E8;
    private static final int BAR_BACK = 0x60000000;
    private static final int SHIELD_LINE = 0xC0F5F1E8;
    private static final int SHIELD_FILL = 0xE0101010;

    private static final int WIDTH = 92;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 6;
    private static final int GAP = 3;
    private static final float TAU = 0.09F;

    private static final String ABILITY_SLOT = "ability";
    private static final String ROAD_SLOT = "road";

    private static final int SHIELD_W = 11;
    private static final int SHIELD_H = 13;
    private static final int HOTBAR_HALF = 91;
    private static final int OFFHAND_CLEARANCE = 30;

    private static final BhAnim.Lift lift = new BhAnim.Lift();
    private static final Map<String, Badge> shown = new LinkedHashMap<>();

    private BhAbilityBadges() {}

    public static void reset() {
        shown.clear();
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
            draw(gfx, font, screenW - WIDTH - MARGIN, y, entry.getValue(), in);
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
        Component value = phase == BhSurge.COOLING
                ? Component.translatable("hud.icys-better-horses.ability.cooling")
                : phase == BhSurge.ARMED
                ? Component.translatable("hud.icys-better-horses.ability.ready")
                : percent > 0
                ? Component.translatable("hud.icys-better-horses.ability.bonus", percent)
                : Component.empty();
        return new Badge(key, label, value, BhSurge.span(packed) > 0 ? BhSurge.fill(packed) : -1.0F,
                phase == BhSurge.COOLING);
    }

    private static void draw(GuiGraphicsExtractor gfx, Font font, int x, int y,
                             Badge badge, float in) {
        float a = BhAnim.clamp01(in);
        if (a <= 0.01F) {
            return;
        }
        int slide = Math.round((1.0F - BhAnim.easeOutCubic(a)) * 14.0F);
        int left = x + slide;

        gfx.fill(left, y, left + WIDTH, y + HEIGHT, BhAnim.fade(BG, a));
        gfx.fill(left, y, left + 2, y + HEIGHT, BhAnim.fade(STRIPE, a));

        chevron(gfx, left + 7, y + 7, BhAnim.fade(badge.cooling ? TEXT : TITLE, a * 0.9F));

        gfx.text(font, badge.label, left + 20, y + 4, BhAnim.fade(TITLE, a), false);
        gfx.text(font, badge.value, left + 20, y + 14, BhAnim.fade(TEXT, a), false);

        if (badge.fill >= 0.0F) {
            int barTop = y + HEIGHT - 3;
            gfx.fill(left + 2, barTop, left + WIDTH, barTop + 2, BhAnim.fade(BAR_BACK, a));
            int span = Math.round((WIDTH - 4) * badge.fill);
            gfx.fill(left + 2, barTop, left + 2 + span, barTop + 2,
                    BhAnim.fade(badge.cooling ? TEXT : TITLE, a));
        }
    }

    private static void chevron(GuiGraphicsExtractor gfx, int x, int y, int color) {
        for (int i = 0; i < 4; i++) {
            gfx.fill(x + i, y - 3 + i, x + i + 1, y + 4 - i, color);
        }
    }

    private static void shield(GuiGraphicsExtractor gfx, int screenW, int screenH, int charge) {
        if (charge < 0) {
            return;
        }
        int x = screenW / 2 + HOTBAR_HALF + OFFHAND_CLEARANCE;
        int y = screenH - 20;
        boolean ready = charge >= 100;

        int line = ready ? TITLE : SHIELD_LINE;
        gfx.fill(x, y, x + SHIELD_W, y + 1, line);
        gfx.fill(x, y, x + 1, y + SHIELD_H - 3, line);
        gfx.fill(x + SHIELD_W - 1, y, x + SHIELD_W, y + SHIELD_H - 3, line);
        gfx.fill(x + 1, y + SHIELD_H - 3, x + SHIELD_W - 1, y + SHIELD_H - 2, line);
        gfx.fill(x + 3, y + SHIELD_H - 2, x + SHIELD_W - 3, y + SHIELD_H - 1, line);

        int inner = SHIELD_H - 4;
        int filled = Math.round(inner * (charge / 100.0F));
        if (filled > 0) {
            gfx.fill(x + 1, y + 1 + inner - filled, x + SHIELD_W - 1, y + 1 + inner,
                    ready ? SHIELD_FILL : BhAnim.fade(SHIELD_FILL, 0.75F));
        }
    }

    private record Badge(String key, Component label, Component value, float fill, boolean cooling) {}

    static {
        BhClientCaches.register(BhAbilityBadges::reset);
    }
}
