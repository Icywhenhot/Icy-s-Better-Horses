package icy.betterhorses.net.client.render;

/**
 * Render state for the Friesian horse.
 *
 * <p>Everything the animator reads lives on {@link BhHorseRenderState}, which is shared by
 * every breed. This type exists so the renderer has a state class of its own — the place
 * to put an input only Friesians need, should one ever turn up.
 */
public class FriesianHorseRenderState extends BhHorseRenderState {
}
