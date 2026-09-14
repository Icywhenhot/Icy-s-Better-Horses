package icy.betterhorses.net.client.book;

import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookTextPageRenderer;
import icy.betterhorses.net.book.BhChargeMeterPage;
import icy.betterhorses.net.client.BhAbilityBadges;
import net.minecraft.client.gui.GuiGraphics;

public class BhChargeMeterPageRenderer extends BookTextPageRenderer {

    private static final int ICON = 16;
    private static final float ZOOM = 1.5F;
    private static final int ICON_Y = BookEntryScreen.PAGE_HEIGHT - 27;
    private static final int STEPS = 10;
    private static final int HOLD = 4;
    private static final long FRAME_MS = 110L;

    public BhChargeMeterPageRenderer(BhChargeMeterPage page) {
        super(page);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        long step = (System.currentTimeMillis() / FRAME_MS) % (STEPS + HOLD);
        int percent = (int) Math.min(step, STEPS) * 100 / STEPS;

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate((BookEntryScreen.PAGE_WIDTH - ICON * ZOOM) / 2.0F, ICON_Y, 0);
        pose.scale(ZOOM, ZOOM, 1);
        guiGraphics.blit(BhAbilityBadges.chargeIcon(percent),
                0, 0, 0.0F, 0.0F, ICON, ICON, ICON, ICON);
        pose.popPose();
    }
}
