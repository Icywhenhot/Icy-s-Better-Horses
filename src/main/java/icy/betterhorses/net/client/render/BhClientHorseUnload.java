package icy.betterhorses.net.client.render;

public final class BhClientHorseUnload {

    private BhClientHorseUnload() {}

    public static void handle(int entityId) {
        int previewId = BhHorseRenderState.previewId(entityId);
        BhEquineGait.remove(entityId);
        BhEquineGait.remove(previewId);
        BhRiderMotion.remove(entityId);
        BhRiderMotion.remove(previewId);
        BhHorseRenderState.remove(entityId);
    }
}
