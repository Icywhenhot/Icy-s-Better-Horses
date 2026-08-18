package icy.betterhorses.net.client.render;

import java.util.HashMap;
import java.util.Map;

/**
 * Where the saddle has been carried to by the horse's animation, relative to where it sits at
 * rest, so the rider's model can be drawn following it.
 *
 * <p><b>Render-only, and that is the whole point.</b> A passenger's entity position drives both
 * its model and its camera, so moving the rider to keep it glued would drag the player's point of
 * view through every bob, bank and jump arc. Instead {@code getPassengerAttachmentPoint} is left
 * exactly as it was - the camera never moves - and only the drawn model is offset.
 *
 * <p>Published per horse id because the rider is a separate entity drawn by a separate renderer:
 * the horse model instance is shared across the breed and the render state is one reused object,
 * so neither can carry it.
 *
 * <p>Ordering caveat: entities render in level order, so on a frame where the rider happens to be
 * drawn before its horse it reads the previous frame's values - about 16 ms stale at 60 fps,
 * against motion that is already smooth. Not worth a second traversal to fix.
 *
 * @param right   blocks along the horse's right, + is right
 * @param up      blocks, + is up
 * @param forward blocks along the horse's facing, + is forward
 * @param pitch   radians, + is nose-down, matching the model's xRot convention
 * @param roll    radians, + leans to the horse's left, matching the model's zRot convention
 */
public record BhRiderMotion(float right, float up, float forward, float pitch, float roll) {

    public static final BhRiderMotion NONE = new BhRiderMotion(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    private static final Map<Integer, BhRiderMotion> ACTIVE = new HashMap<>();

    public static void publish(int horseId, BhRiderMotion motion) {
        ACTIVE.put(horseId, motion);
    }

    public static BhRiderMotion get(int horseId) {
        return ACTIVE.getOrDefault(horseId, NONE);
    }

    /** Wired to client disconnect alongside {@link BhEquineGait#reset()}. */
    public static void reset() {
        ACTIVE.clear();
    }

    public boolean isRest() {
        return right == 0.0F && up == 0.0F && forward == 0.0F && pitch == 0.0F && roll == 0.0F;
    }
}
