package icy.betterhorses.net.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.List;
import java.util.Optional;

public final class BhInventoryEffects {

    private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");

    private BhInventoryEffects() {}

    public static boolean fits(AbstractContainerScreen<?> screen) {
        return screen.width - (screen.getGuiLeft() + screen.getXSize() + 2) >= 32;
    }

    public static void render(AbstractContainerScreen<?> screen, GuiGraphics gfx, Font font, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        int x = screen.getGuiLeft() + screen.getXSize() + 2;
        int room = screen.width - x;
        if (mc.player == null || room < 32) {
            return;
        }
        List<MobEffectInstance> effects = mc.player.getActiveEffects().stream()
                .filter(ForgeHooksClient::shouldRenderEffect).sorted().toList();
        if (effects.isEmpty()) {
            return;
        }
        boolean wide = room >= 120;
        int step = effects.size() > 5 ? 132 / (effects.size() - 1) : 33;

        int y = screen.getGuiTop();
        MobEffectInstance hovered = null;
        for (MobEffectInstance effect : effects) {
            if (wide) {
                gfx.blit(INVENTORY, x, y, 0, 166, 120, 32);
            } else {
                gfx.blit(INVENTORY, x, y, 0, 198, 32, 32);
            }
            gfx.blit(x + (wide ? 6 : 7), y + 7, 0, 18, 18, mc.getMobEffectTextures().get(effect.getEffect()));
            if (wide) {
                gfx.drawString(font, name(effect), x + 28, y + 6, 0xFFFFFF);
                gfx.drawString(font, MobEffectUtil.formatDuration(effect, 1.0F), x + 28, y + 16, 0x7F7F7F);
            } else if (mouseX >= x && mouseX <= x + 33 && mouseY >= y && mouseY <= y + step) {
                hovered = effect;
            }
            y += step;
        }
        if (hovered != null) {
            gfx.renderTooltip(font, List.of(name(hovered), MobEffectUtil.formatDuration(hovered, 1.0F)),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    private static Component name(MobEffectInstance effect) {
        MutableComponent out = effect.getEffect().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            out.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
        }
        return out;
    }
}
