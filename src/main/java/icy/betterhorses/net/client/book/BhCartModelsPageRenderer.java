package icy.betterhorses.net.client.book;

import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.book.BhCartModelsPage;
import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BhCartModelsPageRenderer extends BookPageRenderer<BhCartModelsPage> {

    private static final int TITLE_Y = 0;
    private static final int MODEL_CENTER_Y = 66;
    private static final int MODEL_BOX_HEIGHT = 104;

    private static final int ARROW_WIDTH = 16;
    private static final int ARROW_HEIGHT = 20;
    private static final int ARROW_INSET = 6;
    private static final int ARROW_Y = 112;
    private static final int NAME_Y = ARROW_Y + 6;
    private static final int COUNT_Y = ARROW_Y + ARROW_HEIGHT + 4;
    private static final int NAME_MAX_WIDTH =
            BookEntryScreen.PAGE_WIDTH - 2 * (ARROW_INSET + ARROW_WIDTH) - 8;
    private static final int INK = 0xFF3A2B1B;

    private static final int SMALL_SCALE = 16;
    private static final int LARGE_SCALE = 12;
    private static final float SMALL_HEIGHT = 1.7F;
    private static final float LARGE_HEIGHT = 3.1F;
    private static final float BASE_YAW = 200.0F;
    private static final float SPIN_RANGE = 20.0F;
    private static final float DEG = (float) Math.PI / 180.0F;

    private @Nullable HorseCartEntity cart;
    private int index;
    private boolean errored;

    public BhCartModelsPageRenderer(BhCartModelsPage page) {
        super(page);
    }

    @Override
    public void onBeginDisplayPage(BookEntryScreen parentScreen, int left, int top) {
        super.onBeginDisplayPage(parentScreen, left, top);

        loadCart();
        if (cart == null) {
            return;
        }

        addButton(Button.builder(Component.literal("<"), button -> cycle(-1))
                .bounds(ARROW_INSET, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)
                .build());
        addButton(Button.builder(Component.literal(">"), button -> cycle(1))
                .bounds(BookEntryScreen.PAGE_WIDTH - ARROW_WIDTH - ARROW_INSET,
                        ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        centeredFitted(guiGraphics,
                Component.translatable("book.icys-better-horses.carts.title").getString(),
                TITLE_Y, BookEntryScreen.PAGE_WIDTH);

        if (cart == null) {
            centeredFitted(guiGraphics,
                    Component.translatable("book.icys-better-horses.coats.unavailable").getString(),
                    MODEL_CENTER_Y, BookEntryScreen.PAGE_WIDTH);
            return;
        }

        renderCart(guiGraphics, mouseX, mouseY);

        centeredFitted(guiGraphics, Component.translatable(nameKey()).getString(),
                NAME_Y, NAME_MAX_WIDTH);
        centeredFitted(guiGraphics, (index + 1) + " / " + CartSize.values().length,
                COUNT_Y, BookEntryScreen.PAGE_WIDTH);
    }

    private String nameKey() {
        return "book.icys-better-horses.carts."
                + (CartSize.values()[index].isLarge() ? "large" : "small");
    }

    private void centeredFitted(GuiGraphicsExtractor guiGraphics, String text, int y, int maxWidth) {
        int width = this.font.width(text);
        int centerX = BookEntryScreen.PAGE_WIDTH / 2;
        if (width <= maxWidth) {
            guiGraphics.text(this.font, text, centerX - width / 2, y, INK, false);
            return;
        }

        float scale = maxWidth / (float) width;
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, (float) y);
        pose.scale(scale, scale);
        guiGraphics.text(this.font, text, -width / 2, 0, INK, false);
        pose.popMatrix();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderCart(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (errored || cart == null) {
            return;
        }

        int pageX = this.parentScreen.getBookLeft() + this.left;
        int pageY = this.parentScreen.getBookTop() + this.top;
        int x0 = pageX;
        int x1 = pageX + BookEntryScreen.PAGE_WIDTH;
        int y0 = pageY + MODEL_CENTER_Y - MODEL_BOX_HEIGHT / 2;
        int y1 = y0 + MODEL_BOX_HEIGHT;

        float spin = (float) Math.atan(((x0 + x1) / 2.0F - mouseX) / 40.0F);
        float lean = (float) Math.atan(((y0 + y1) / 2.0F - mouseY) / 40.0F);
        Quaternionf flip = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf pitch = new Quaternionf().rotateX(lean * SPIN_RANGE * DEG);
        flip.mul(pitch);

        CartSize size = CartSize.values()[index];
        float yaw = BASE_YAW + spin * SPIN_RANGE;
        cart.setYRot(yaw);
        cart.setYBodyRot(yaw);
        cart.setYHeadRot(yaw);

        try {
            EntityRenderer renderer = this.mc.getEntityRenderDispatcher().getRenderer(cart);
            EntityRenderState state = renderer.createRenderState(cart, 1.0F);
            state.shadowPieces.clear();
            state.outlineColor = 0;

            Vec3 shift = new Vec3(0.0D, 0.0D, size.bedCenterBehind()).yRot(-yaw * DEG).scale(-1.0D);
            Vector3f offset = new Vector3f((float) shift.x,
                    (size.isLarge() ? LARGE_HEIGHT : SMALL_HEIGHT) / 2.0F,
                    (float) shift.z);

            guiGraphics.entity(state, size.isLarge() ? LARGE_SCALE : SMALL_SCALE,
                    offset, flip, pitch, x0, y0, x1, y1);
        } catch (Exception exception) {
            errored = true;
            IcysBetterHorses.LOGGER.warn("[handbook] could not draw the cart preview", exception);
        }
    }

    private void cycle(int direction) {
        if (cart == null) {
            return;
        }
        index = Math.floorMod(index + direction, CartSize.values().length);
        cart.setSize(CartSize.values()[index]);
    }

    private void loadCart() {
        if (cart != null || errored) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        try {
            cart = HorseCartEntity.preview(minecraft.level, CartSize.values()[index]);
        } catch (Exception exception) {
            errored = true;
            IcysBetterHorses.LOGGER.warn("[handbook] could not build the cart preview", exception);
        }
    }
}
