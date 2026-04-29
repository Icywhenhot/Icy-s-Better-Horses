package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RadialMenuScreen extends Screen {

    private static final int SEGMENT_COUNT = 4;
    private static final int OPTION_WIDTH = 112;
    private static final int OPTION_HEIGHT = 28;
    private static final int OPTION_OFFSET = 56;
    private static final int BASE_BACKGROUND_COLOR = 0x70070A12;
    private static final int PANEL_COLOR = 0xC01A2130;
    private static final int PANEL_HOVER_COLOR = 0xFFE0EDFF;
    private static final int PANEL_BORDER_COLOR = 0xFF6E819B;
    private static final int PANEL_BORDER_HOVER_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFD4DAE6;
    private static final int TEXT_HOVER_COLOR = 0xFF0E1827;
    private static final int CENTER_COLOR = 0xE03D4A63;

    private static final HorseCommand[] COMMANDS = {
            HorseCommand.FOLLOW,
            HorseCommand.SET_HOME,
            HorseCommand.STAY,
            HorseCommand.RETURN_HOME,
    };

    private static final int[] LABEL_DX = {0, 1, 0, -1};
    private static final int[] LABEL_DY = {-1, 0, 1, 0};

    private final int horseId;
    private int hoveredIndex = -1;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys-better-horses.radial"));
        this.horseId = horseId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.hoveredIndex = this.findHoveredIndex(cx, cy, mouseX, mouseY);

        gfx.fill(0, 0, this.width, this.height, BASE_BACKGROUND_COLOR);
        gfx.fill(cx - 24, cy - 24, cx + 24, cy + 24, CENTER_COLOR);
        gfx.drawCenteredString(this.font, this.title, cx, cy - this.font.lineHeight / 2, 0xFFFFFFFF);

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            OptionBounds bounds = this.boundsFor(cx, cy, i);
            boolean hovered = i == this.hoveredIndex;
            int fillColor = hovered ? PANEL_HOVER_COLOR : PANEL_COLOR;
            int borderColor = hovered ? PANEL_BORDER_HOVER_COLOR : PANEL_BORDER_COLOR;
            int textColor = hovered ? TEXT_HOVER_COLOR : TEXT_COLOR;

            gfx.fill(bounds.left, bounds.top, bounds.right, bounds.bottom, borderColor);
            gfx.fill(bounds.left + 1, bounds.top + 1, bounds.right - 1, bounds.bottom - 1, fillColor);
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable(this.commandKey(COMMANDS[i])),
                    (bounds.left + bounds.right) / 2,
                    bounds.top + (OPTION_HEIGHT - this.font.lineHeight) / 2,
                    textColor);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.hoveredIndex >= 0) {
            this.sendCommand(COMMANDS[this.hoveredIndex]);
            this.onClose();
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_P) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    private int findHoveredIndex(int cx, int cy, int mouseX, int mouseY) {
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            OptionBounds bounds = this.boundsFor(cx, cy, i);
            if (mouseX >= bounds.left && mouseX < bounds.right && mouseY >= bounds.top && mouseY < bounds.bottom) {
                return i;
            }
        }

        return -1;
    }

    private OptionBounds boundsFor(int cx, int cy, int index) {
        int centerX = cx + LABEL_DX[index] * OPTION_OFFSET;
        int centerY = cy + LABEL_DY[index] * OPTION_OFFSET;
        return new OptionBounds(
                centerX - OPTION_WIDTH / 2,
                centerY - OPTION_HEIGHT / 2,
                centerX + OPTION_WIDTH / 2,
                centerY + OPTION_HEIGHT / 2);
    }

    private void sendCommand(HorseCommand command) {
        ClientPlayNetworking.send(new RadialCommandPayload(this.horseId, command.ordinal()));
    }

    private String commandKey(HorseCommand command) {
        return switch (command) {
            case FOLLOW -> "command.icys-better-horses.follow";
            case STAY -> "command.icys-better-horses.stay";
            case RETURN_HOME -> "command.icys-better-horses.return_home";
            case SET_HOME -> "command.icys-better-horses.set_home";
        };
    }

    private record OptionBounds(int left, int top, int right, int bottom) {}
}
