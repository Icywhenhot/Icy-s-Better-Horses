package icy.betterhorses.net.client;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

public class BhJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addAfterRenderCallback((box, rect, gfx, accessor) -> JadeTooltipBox.shown(rect.expectedRect.getX(),
                rect.expectedRect.getY(), rect.expectedRect.getWidth(), rect.expectedRect.getHeight()));
    }
}
