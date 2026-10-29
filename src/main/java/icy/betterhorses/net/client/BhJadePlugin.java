package icy.betterhorses.net.client;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

public class BhJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addAfterRenderCallback((box, animation, gfx, accessor) -> JadeTooltipBox.shown(animation.expectedRect.getX(),
                animation.expectedRect.getY(), animation.expectedRect.getWidth(), animation.expectedRect.getHeight()));
    }
}
