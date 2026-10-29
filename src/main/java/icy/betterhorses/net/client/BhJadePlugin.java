package icy.betterhorses.net.client;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

public class BhJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addAfterRenderCallback((tooltip, rect, gfx, accessor) ->
                JadeTooltipBox.shown(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
    }
}
