package icy.betterhorses.net.client.book;

import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.book.BhBreedCoatsPage;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class BhBreedCoatsPageRenderer extends BookPageRenderer<BhBreedCoatsPage> {

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

    private static final int MIN_SCALE = 18;
    private static final int MAX_SCALE = 46;
    private static final float MODEL_FILL = 0.7F;

    private static final float VISUAL_HEIGHT_FACTOR = 1.5F;

    private @Nullable BhBreedHorse horse;
    private int coatIndex;
    private boolean errored;

    public BhBreedCoatsPageRenderer(BhBreedCoatsPage page) {
        super(page);
    }

    @Override
    public void onBeginDisplayPage(BookEntryScreen parentScreen, int left, int top) {
        super.onBeginDisplayPage(parentScreen, left, top);

        loadHorse();
        if (horse == null || coatCount() <= 1) {
            return;
        }

        addButton(Button.builder(Component.literal("<"), button -> cycleCoat(-1))
                .bounds(ARROW_INSET, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)
                .build());
        addButton(Button.builder(Component.literal(">"), button -> cycleCoat(1))
                .bounds(BookEntryScreen.PAGE_WIDTH - ARROW_WIDTH - ARROW_INSET,
                        ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        centeredFitted(
                guiGraphics,
                Component.translatable("book.icys-better-horses.coats.title").getString(),
                TITLE_Y,
                BookEntryScreen.PAGE_WIDTH);

        if (horse == null) {
            centeredFitted(
                    guiGraphics,
                    Component.translatable("book.icys-better-horses.coats.unavailable").getString(),
                    MODEL_CENTER_Y,
                    BookEntryScreen.PAGE_WIDTH);
            return;
        }

        renderHorse(guiGraphics, mouseX, mouseY);

        centeredFitted(guiGraphics, horse.bhCoats().displayName(coatIndex).getString(),
                NAME_Y, NAME_MAX_WIDTH);
        centeredFitted(guiGraphics, (coatIndex + 1) + " / " + coatCount(),
                COUNT_Y, BookEntryScreen.PAGE_WIDTH);
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

    private void renderHorse(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (errored || horse == null) {
            return;
        }

        int pageX = this.parentScreen.getBookLeft() + this.left;
        int pageY = this.parentScreen.getBookTop() + this.top;
        int x0 = pageX;
        int x1 = pageX + BookEntryScreen.PAGE_WIDTH;
        int y0 = pageY + MODEL_CENTER_Y - MODEL_BOX_HEIGHT / 2;
        int y1 = y0 + MODEL_BOX_HEIGHT;

        float boxWidth = Math.max(0.1F, horse.getBbWidth());
        float visualHeight = Math.max(0.1F, horse.getBbHeight() * VISUAL_HEIGHT_FACTOR);
        int scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, (int) Math.min(
                MODEL_BOX_HEIGHT * MODEL_FILL / visualHeight,
                BookEntryScreen.PAGE_WIDTH * MODEL_FILL / boxWidth)));

        try {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    guiGraphics, x0, y0, x1, y1, scale, 0.0F, mouseX, (y0 + y1) / 2f, horse);
        } catch (Exception exception) {
            errored = true;
            IcysBetterHorses.LOGGER.warn("[handbook] could not draw the coat preview for {}",
                    this.getPage().getEntityId(), exception);
        }
    }

    private void cycleCoat(int direction) {
        if (horse == null) {
            return;
        }
        int count = coatCount();
        coatIndex = Math.floorMod(coatIndex + direction, count);
        horse.bhSetCoat(coatIndex);
    }

    private int coatCount() {
        return horse == null ? 0 : horse.bhCoats().count();
    }

    private void loadHorse() {
        if (horse != null || errored) {
            return;
        }

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Identifier typeId = Identifier.tryParse(this.getPage().getEntityId());
        EntityType<?> type = typeId == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(typeId);
        if (type == null) {
            markErrored("unknown entity type " + this.getPage().getEntityId(), null);
            return;
        }

        Entity created;
        try {
            created = type.create(minecraft.level, EntitySpawnReason.LOAD);
        } catch (Exception exception) {
            markErrored("could not build " + this.getPage().getEntityId(), exception);
            return;
        }

        if (!(created instanceof BhBreedHorse breedHorse)) {
            markErrored(this.getPage().getEntityId() + " is not a breed mob", null);
            return;
        }

        breedHorse.setId(-1);
        breedHorse.setCustomNameVisible(false);
        breedHorse.bhSetCoat(0);
        horse = breedHorse;
        coatIndex = 0;
    }

    private void markErrored(String message, @Nullable Exception exception) {
        errored = true;
        if (exception == null) {
            IcysBetterHorses.LOGGER.warn("[handbook] coat page: {}", message);
        } else {
            IcysBetterHorses.LOGGER.warn("[handbook] coat page: {}", message, exception);
        }
    }
}
