package icy.betterhorses.net;

public final class HorseStabilizerLogic {
    public static final float HALF_OPEN_FALL_DISTANCE = 4.0F;
    public static final float OPEN_FALL_DISTANCE = 6.0F;
    public static final double MIN_DEPLOY_DESCENT_SPEED = -0.08D;
    public static final double MIN_SUSTAINED_DESCENT_SPEED = -0.02D;

    /**
     * Note: {@code verticalSpeed} is retained in the signature for backwards compatibility but is
     * no longer consulted. On a player-ridden horse the server never updates the horse's own
     * {@code deltaMovement.y} (the rider's client drives positional movement and the horse is
     * dragged along), so {@code verticalSpeed} stays at {@code 0.0} the entire fall — which
     * previously caused the deploy gate to short-circuit to CLOSED and the wings to never open.
     * {@code fallDistance} is updated correctly via positional deltas and is the reliable signal.
     */
    public static HorseStabilizerState computeState(boolean equipped, boolean onGround, boolean inWater,
                                                    boolean inLava, boolean passenger, double verticalSpeed,
                                                    float fallDistance, HorseStabilizerState currentState) {
        if (!equipped || onGround || inWater || inLava || passenger) {
            return HorseStabilizerState.CLOSED;
        }

        if (currentState == HorseStabilizerState.OPEN) {
            return HorseStabilizerState.OPEN;
        }

        if (currentState == HorseStabilizerState.HALF_OPEN) {
            if (fallDistance >= OPEN_FALL_DISTANCE) {
                return HorseStabilizerState.OPEN;
            }
            return HorseStabilizerState.HALF_OPEN;
        }

        if (fallDistance >= OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.OPEN;
        }

        if (fallDistance >= HALF_OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.HALF_OPEN;
        }

        return HorseStabilizerState.CLOSED;
    }

    public static HorseStabilizerState resolveLandingState(boolean equipped, float fallDistance,
                                                           HorseStabilizerState currentState) {
        if (!equipped) {
            return HorseStabilizerState.CLOSED;
        }

        if (currentState != HorseStabilizerState.CLOSED) {
            return currentState;
        }

        if (fallDistance >= OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.OPEN;
        }

        if (fallDistance >= HALF_OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.HALF_OPEN;
        }

        return HorseStabilizerState.CLOSED;
    }

    public static boolean shouldPlaySteam(HorseStabilizerState state) {
        return state == HorseStabilizerState.OPEN;
    }

    private HorseStabilizerLogic() {}
}
