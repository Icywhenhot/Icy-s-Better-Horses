package icy.betterhorses.net.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.client.ICuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

final class CuriosLink {

    private static final ResourceLocation TEXTURE = new ResourceLocation("curios", "textures/gui/inventory.png");

    private CuriosLink() {}

    static AbstractWidget button(int x, int y) {
        ImageButton button = new ImageButton(x, y, 14, 14, 50, 0, 14, TEXTURE, b -> open());
        button.setTooltip(Tooltip.create(Component.translatable("gui.icys-better-horses.rider_panel.curios")));
        return button;
    }

    static boolean isCuriosScreen(Screen screen) {
        return screen instanceof ICuriosScreen;
    }

    static int panelWidth(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> gui)) {
            return 0;
        }
        if (gui.getMenu() instanceof CuriosContainerV2 menu) {
            return menu.panelWidth;
        }
        if (gui.getMenu() instanceof CuriosContainer menu) {
            return 26 + (menu.hasCosmeticColumn() ? 19 : 0) + (menu.canScroll() ? 16 : 0);
        }
        return 0;
    }

    private static void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack carried = player.containerMenu.getCarried();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        RiderPanel.rememberCursor();
        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CPacketOpenCurios(carried));
    }
}
