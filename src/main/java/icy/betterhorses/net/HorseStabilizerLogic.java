package icy.betterhorses.net;

public final class HorseStabilizerLogic {
    public static final float HALF_OPEN_FALL_DISTANCE = 4.0F;
    public static final float OPEN_FALL_DISTANCE = 6.0F;
    public static final double MIN_DEPLOY_DESCENT_SPEED = -0.08D;
    public static final double MIN_SUSTAINED_DESCENT_SPEED = -0.02D;

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
            if (fallDistance >= OPEN_FALL_DISTANCE && verticalSpeed < MIN_SUSTAINED_DESCENT_SPEED) {
                return HorseStabilizerState.OPEN;
            }

            return verticalSpeed < MIN_SUSTAINED_DESCENT_SPEED
                    ? HorseStabilizerState.HALF_OPEN
                    : HorseStabilizerState.CLOSED;
        }

        if (verticalSpeed >= MIN_DEPLOY_DESCENT_SPEED) {
            return HorseStabilizerState.CLOSED;
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
