package icy.betterhorses.net.client.render;

/**
 * Lets a rider's render state remember which horse it is sitting on, so the rider's model can be
 * offset to follow that horse's animated saddle.
 *
 * <p>Mixed into {@code LivingEntityRenderState}, which otherwise carries nothing about vehicles.
 */
public interface IBhRiderState {

    /** @param horseId the ridden horse's entity id, or -1 if not riding one */
    void bh_setRiddenHorse(int horseId, float bodyYaw);

    int bh_getRiddenHorseId();

    /** The horse's interpolated body yaw in degrees, for turning saddle-space into world space. */
    float bh_getRiddenHorseYaw();
}
