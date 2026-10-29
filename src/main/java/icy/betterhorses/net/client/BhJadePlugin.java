package icy.betterhorses.net.client;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("icys_better_horses")
public class BhJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addAfterRenderCallback((tooltip, rect, gfx, accessor) ->
                JadeTooltipBox.shown(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
    }
}
