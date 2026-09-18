package icy.betterhorses.net.client;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import icy.betterhorses.net.ModItems;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BhAbilityBadges {

    private static final int INK = 0xFF3B200B;
    private static final int INK_SOFT = 0xFF6B4A2A;
    private static final int FILL = 0xFFBF360C;
    private static final int FILL_COOL = 0xFFAD8865;

    private static final int HEIGHT = 30;
    private static final int MARGIN = 6;
    private static final int GAP = 5;
    private static final float SETTLE = 0.25F;
    private static final float TAU = 0.09F;

    private static final int ICON = 16;
    private static final int ICON_Y = 7;
    private static final int TEXT_Y = 7;
    private static final int TEXT_W = 70;
    private static final float TEXT_SCALE = 0.75F;
    private static final float MIN_TEXT_SCALE = 0.6F;
    private static final int BAR_X = 28;
    private static final int BAR_W = 66;
    private static final int VALUE_Y = 18;

    private static final ResourceLocation BAR = new ResourceLocation(
            "icys-better-horses", "textures/gui/hud/badge_fill.png");

    private static final Plate WIDE = new Plate(new ResourceLocation(
            "icys-better-horses", "textures/gui/hud/badge_plate.png"), 102, 7, 27, TEXT_W);
    private static final Plate NARROW = new Plate(new ResourceLocation(
            "icys-better-horses", "textures/gui/hud/badge_plate_no_bar.png"), 70, 8, 28, 37);
    private static final Map<String, Boolean> found = new HashMap<>();

    private static final Map<String, ItemStack> STOCK_ICONS = Map.ofEntries(
            Map.entry("belgian", new ItemStack(Items.BRICK)),
            Map.entry("percheron", new ItemStack(Items.COBWEB)),
            Map.entry("icelandic_2", new ItemStack(Items.COBWEB)),
            Map.entry("shire", new ItemStack(Items.SHIELD)),
            Map.entry("shire_1", new ItemStack(Items.BRICK)),
            Map.entry("appaloosa", new ItemStack(Items.WHEAT)),
            Map.entry("appaloosa_1", new ItemStack(Items.CARROT)),
            Map.entry("appaloosa_2", new ItemStack(Items.GOLDEN_CARROT)),
            Map.entry("friesian_1", new ItemStack(Items.POPPY)),
            Map.entry("andalusian_1", new ItemStack(Items.IRON_CHESTPLATE)),
            Map.entry("mustang_1", new ItemStack(Items.GLISTERING_MELON_SLICE)),
            Map.entry("clydesdale", new ItemStack(Items.IRON_HORSE_ARMOR)),
            Map.entry("clydesdale_1", new ItemStack(Items.ARROW)),
            Map.entry("american_paint", new ItemStack(Items.COMPASS)),
            Map.entry("haflinger_1", new ItemStack(ModItems.HORSE_CART.get())),
            Map.entry("perk_1", new ItemStack(Items.GOLDEN_APPLE)),
            Map.entry("perk_2", new ItemStack(Items.POWDER_SNOW_BUCKET)),
            Map.entry("perk_3", new ItemStack(Items.FEATHER)));

    private static final String ROAD_SLOT = "road";

    private static final int BASH_SIZE = 16;
    private static final int BASH_FRAMES = 10;
    private static final int HOTBAR_HALF = 91;
    private static final int HOTBAR_GAP = 4;
    private static final int OFFHAND_WIDTH = 29;
    private static final int HOTBAR_HEIGHT = 22;

    private static final ResourceLocation[] BASH = new ResourceLocation[BASH_FRAMES + 1];

    static {
        for (int i = 0; i <= BASH_FRAMES; i++) {
            BASH[i] = new ResourceLocation(
                    "icys-better-horses", "textures/gui/hud/bash_" + i + ".png");
        }
    }

    private static final BhAnim.Lift lift = new BhAnim.Lift();
    private static final Map<String, Badge> shown = new LinkedHashMap<>();
    private static final Map<String, Float> slide = new HashMap<>();

    private BhAbilityBadges() {}

    public static void reset() {
        shown.clear();
        slide.clear();
        found.clear();
    }

    public static void render(GuiGraphics gfx, Font font, int screenW, int screenH,
                              AbstractHorse horse) {
        IHorseData data = IHorseData.of(horse);
        HorseBreed breed = data.bh_getBreed();
        if (!breed.isRealBreed()) {
            return;
        }

        Set<String> live = new HashSet<>();
        for (Badge badge : new Badge[]{
                read(breed, data.bh_getSurge(), false),
                read(breed, data.bh_getPulse(), false),
                read(breed, data.bh_getPerkSurge(), true)}) {
            if (badge != null) {
                shown.put(badge.key, badge);
                live.add(badge.key);
            }
        }

        lift.beginFrame(TAU);
        float top = screenH / 2.0F - 30.0F;
        int rank = 0;
        Iterator<Map.Entry<String, Badge>> it = shown.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Badge> entry = it.next();
            String key = entry.getKey();
            boolean up = live.contains(key);
            float in = lift.get(key, up, 1.0F);
            if (!up && in <= 0.02F) {
                it.remove();
                slide.remove(key);
                continue;
            }
            float want = top + rank * (HEIGHT + GAP);
            float at = slide.getOrDefault(key, want);
            at += (want - at) * SETTLE;
            slide.put(key, at);

            Badge badge = entry.getValue();
            Plate plate = badge.fill < 0.0F ? NARROW : WIDE;
            draw(gfx, font, screenW - plate.width() - MARGIN, Math.round(at), badge, plate, in);
            rank++;
        }

        shield(gfx, screenW, screenH, data.bh_getCharge());
    }

    private static @Nullable Badge read(HorseBreed breed, int packed, boolean road) {
        int phase = BhSurge.phase(packed);
        if (phase == BhSurge.IDLE) {
            return null;
        }
        int variant = BhSurge.variant(packed);
        String key = road
                ? (variant == 0 ? ROAD_SLOT : "perk_" + variant)
                : breed.name().toLowerCase(Locale.ROOT) + (variant == 0 ? "" : "_" + variant);
        Component label = Component.translatable("hud.icys-better-horses.ability." + key);
        int percent = BhSurge.percent(packed);
        Component value = switch (phase) {
            case BhSurge.COOLING -> Component.translatable("hud.icys-better-horses.ability.cooling");
            case BhSurge.ARMED -> Component.translatable("hud.icys-better-horses.ability.ready");
            default -> percent > 0
                    ? Component.translatable("hud.icys-better-horses.ability.bonus", percent)
                    : Component.empty();
        };
        boolean metered = (phase == BhSurge.ACTIVE || phase == BhSurge.COOLING)
                && BhSurge.span(packed) > 0;
        return new Badge(key, label, value, metered ? BhSurge.fill(packed) : -1.0F,
                phase == BhSurge.COOLING);
    }

    private static void draw(GuiGraphics gfx, Font font, int x, int y,
                             Badge badge, Plate plate, float in) {
        float a = BhAnim.clamp01(in);
        if (a <= 0.01F) {
            return;
        }
        int slide = Math.round((1.0F - BhAnim.easeOutCubic(a)) * 14.0F);
        int left = x + slide;
        int tint = (Math.round(255.0F * a) << 24) | 0xFFFFFF;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        tint(gfx, tint);
        gfx.blit(plate.tex(), left, y, 0.0F, 0.0F, plate.width(), HEIGHT, plate.width(), HEIGHT);

        ResourceLocation own = iconId(badge.key);
        if (present(own)) {
            gfx.blit(own, left + plate.iconX(), y + ICON_Y, 0.0F, 0.0F, ICON, ICON, ICON, ICON);
        } else {
            ItemStack stock = STOCK_ICONS.get(badge.key);
            if (stock != null) {
                gfx.renderItem(stock, left + plate.iconX(), y + ICON_Y);
            }
        }
        gfx.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        float labelScale = fit(font, badge.label, plate.textW());
        small(gfx, font, badge.label, left + plate.textX(), y + TEXT_Y, BhAnim.fade(INK, a), labelScale);

        int rawValue = font.width(badge.value);
        if (rawValue <= 0) {
            return;
        }
        if (badge.fill < 0.0F) {
            small(gfx, font, badge.value, left + plate.textX(), y + VALUE_Y,
                    BhAnim.fade(INK_SOFT, a), fit(font, badge.value, plate.textW()));
            return;
        }
        int labelWidth = Math.round(font.width(badge.label) * labelScale);
        int valueWidth = Math.round(rawValue * TEXT_SCALE);
        if (labelWidth + valueWidth + 3 <= plate.textW()) {
            small(gfx, font, badge.value, left + plate.textX() + plate.textW() - valueWidth,
                    y + TEXT_Y, BhAnim.fade(INK_SOFT, a), TEXT_SCALE);
        }

        int span = Math.round(BAR_W * badge.fill);
        if (span > 0) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            tint(gfx, BhAnim.fade(badge.cooling ? FILL_COOL : FILL, a));
            gfx.blit(BAR, left + BAR_X, y, BAR_X, 0.0F, span, HEIGHT, WIDE.width(), HEIGHT);
            RenderSystem.disableBlend();
        }
        gfx.setColor(1f, 1f, 1f, 1f);
    }

    private static float fit(Font font, Component text, int room) {
        int w = font.width(text);
        if (w <= 0) {
            return TEXT_SCALE;
        }
        return Math.max(MIN_TEXT_SCALE, Math.min(TEXT_SCALE, room / (float) w));
    }

    private static void small(GuiGraphics gfx, Font font, Component text,
                              int x, int y, int color, float scale) {
        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1);
        gfx.drawString(font, text, 0, 0, color, false);
        pose.popPose();
    }

    public static ResourceLocation chargeIcon(int percent) {
        return BASH[Mth.clamp(Math.round(percent * BASH_FRAMES / 100.0F), 0, BASH_FRAMES)];
    }

    private static ResourceLocation iconId(String key) {
        return new ResourceLocation(
                "icys-better-horses", "textures/gui/hud/icon_" + key + ".png");
    }

    private static boolean present(ResourceLocation id) {
        return found.computeIfAbsent(id.toString(), k -> Minecraft.getInstance()
                .getResourceManager().getResource(id).isPresent());
    }

    private static void shield(GuiGraphics gfx, int screenW, int screenH, int charge) {
        if (charge < 0) {
            return;
        }
        boolean lefty = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT;
        int x = screenW / 2 + HOTBAR_HALF + HOTBAR_GAP + (lefty ? OFFHAND_WIDTH : 0);
        int y = screenH - HOTBAR_HEIGHT + (HOTBAR_HEIGHT - BASH_SIZE) / 2;
        int frame = Mth.clamp(Math.round(charge * BASH_FRAMES / 100.0F), 0, BASH_FRAMES);

        gfx.blit(BASH[frame], x, y,
                0.0F, 0.0F, BASH_SIZE, BASH_SIZE, BASH_SIZE, BASH_SIZE);
    }

    private static void tint(GuiGraphics gfx, int color) {
        gfx.setColor(
                ((color >> 16) & 255) / 255f,
                ((color >> 8) & 255) / 255f,
                (color & 255) / 255f,
                ((color >>> 24) & 255) / 255f);
    }

    private record Badge(String key, Component label, Component value, float fill, boolean cooling) {}

    private record Plate(ResourceLocation tex, int width, int iconX, int textX, int textW) {}

    static {
        BhClientCaches.register(BhAbilityBadges::reset);
    }
}
