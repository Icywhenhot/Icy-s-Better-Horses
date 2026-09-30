package icy.betterhorses.net.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import top.theillusivec4.curios.api.client.ICuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

final class CuriosLink {

    private CuriosLink() {}

    static boolean isCuriosScreen(Screen screen) {
        return screen instanceof ICuriosScreen;
    }

    static int panelWidth(Screen screen) {
        return screen instanceof AbstractContainerScreen<?> gui && gui.getMenu() instanceof CuriosContainer menu
                ? menu.panelWidth
                : 0;
    }
}
