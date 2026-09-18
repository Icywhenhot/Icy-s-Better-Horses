package icy.betterhorses.net.client.book;

import com.mojang.blaze3d.systems.RenderSystem;
import icy.betterhorses.net.client.BhAbilityBadges;
import net.minecraft.client.gui.GuiGraphics;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.function.UnaryOperator;

public class BhChargeMeterComponent implements ICustomComponent {

    private static final int PAGE_WIDTH = 116;
    private static final int ICON = 16;
    private static final float ZOOM = 1.5F;
    private static final int STEPS = 10;
    private static final int HOLD = 4;
    private static final long FRAME_MS = 110L;

    private transient int x;
    private transient int y;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
    }

    @Override
    public void build(int x, int y, int pageNum) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(GuiGraphics gfx, IComponentRenderContext context,
                       float partialTicks, int mouseX, int mouseY) {
        long step = (System.currentTimeMillis() / FRAME_MS) % (STEPS + HOLD);
        int percent = (int) Math.min(step, STEPS) * 100 / STEPS;

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(this.x + (PAGE_WIDTH - ICON * ZOOM) / 2.0F, this.y, 0.0F);
        pose.scale(ZOOM, ZOOM, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.blit(BhAbilityBadges.chargeIcon(percent), 0, 0, 0.0F, 0.0F, ICON, ICON, ICON, ICON);
        RenderSystem.disableBlend();
        pose.popPose();
    }
}
