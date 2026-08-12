package icy.betterhorses.net.client.render;

/**
 * Render state for the medium size class.
 *
 * <p>Shared by every medium breed rather than one per breed, because nothing on it is
 * breed-specific — the coat arrives as a texture in {@code coatTexture}, which is all the
 * renderer needs to tell an Appaloosa from a Thoroughbred.
 */
public class MediumHorseRenderState extends BhHorseRenderState {
}
