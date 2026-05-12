package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique private static final int BH_STATS_HUD_TOP = 12;
    @Unique private static final int BH_STATS_HUD_PADDING = 6;
    @Unique private static final int BH_STATS_HUD_TEXT_COLOR = 0xFFF5F1E8;
    @Unique private static final int BH_STATS_HUD_TITLE_COLOR = 0xFFF2C15B;
    @Unique private static final int BH_STATS_HUD_BACKGROUND = 0xA0101010;
    @Unique private static final int BH_STATS_HUD_ACCENT = 0xD06E5324;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bh_renderHorseStatsHud(GuiGraphicsExtractor gfx, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.player == null || this.minecraft.level == null || this.minecraft.screen != null) {
            return;
        }
        if (!this.bh_isHoldingUpgradedSaddle()) {
            return;
        }
        if (!(this.minecraft.crosshairPickEntity instanceof AbstractHorse horse)) {
            return;
        }

        String speedValue = String.format(Locale.ROOT, "%.1f",
                horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.2D);
        String jumpValue = String.format(Locale.ROOT, "%.1f",
                Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D));

        Component title = Component.translatable("hud.icys-better-horses.horse_stats");
        Component speedLine = Component.translatable("hud.icys-better-horses.speed", speedValue);
        Component jumpLine = Component.translatable("hud.icys-better-horses.jump", jumpValue);

        int lineHeight = this.minecraft.font.lineHeight + 2;
        int boxWidth = Math.max(this.minecraft.font.width(title),
                Math.max(this.minecraft.font.width(speedLine), this.minecraft.font.width(jumpLine)))
                + BH_STATS_HUD_PADDING * 2;
        int boxHeight = BH_STATS_HUD_PADDING * 2 + lineHeight * 3;
        int left = (this.minecraft.getWindow().getGuiScaledWidth() - boxWidth) / 2;
        int top = BH_STATS_HUD_TOP;

        gfx.fill(left, top, left + boxWidth, top + boxHeight, BH_STATS_HUD_BACKGROUND);
        gfx.fill(left, top, left + boxWidth, top + 2, BH_STATS_HUD_ACCENT);

        int textX = left + BH_STATS_HUD_PADDING;
        int textY = top + BH_STATS_HUD_PADDING;
        gfx.text(this.minecraft.font, title, textX, textY, BH_STATS_HUD_TITLE_COLOR, false);
        gfx.text(this.minecraft.font, speedLine, textX, textY + lineHeight, BH_STATS_HUD_TEXT_COLOR, false);
        gfx.text(this.minecraft.font, jumpLine, textX, textY + lineHeight * 2, BH_STATS_HUD_TEXT_COLOR, false);
    }

    @Unique
    private boolean bh_isHoldingUpgradedSaddle() {
        return this.minecraft.player != null
                && (this.minecraft.player.getMainHandItem().is(ModItems.UPGRADED_SADDLE)
                || this.minecraft.player.getOffhandItem().is(ModItems.UPGRADED_SADDLE));
    }
}
