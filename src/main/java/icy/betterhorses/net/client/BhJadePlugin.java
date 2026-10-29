package icy.betterhorses.net.client;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class BhJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addAfterRenderCallback((box, rect, gfx, accessor) -> JadeTooltipBox.shown(rect.expectedRect));
    }
}
