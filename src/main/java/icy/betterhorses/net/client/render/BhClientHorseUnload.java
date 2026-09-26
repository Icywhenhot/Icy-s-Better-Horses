package icy.betterhorses.net.client.render;

// Round 2, item 12: these per-entity client render caches never dropped an entry when the horse
// they belonged to unloaded, leaking one map slot per horse ever seen for the life of the client.
public final class BhClientHorseUnload {

    private BhClientHorseUnload() {}

    public static void handle(int entityId) {
        BhEquineGait.remove(entityId);
        BhEquineGait.remove(BhHorseRenderState.previewId(entityId)); // gait state is keyed by preview id too
        BhRiderMotion.remove(entityId);
        BhHorseRenderState.remove(entityId);
    }
}
