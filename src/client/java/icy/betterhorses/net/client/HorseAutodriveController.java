package icy.betterhorses.net.client;

public final class HorseAutodriveController {
    public static final HorseAutodriveController INSTANCE = new HorseAutodriveController();
    public static final int DOUBLE_TAP_WINDOW_TICKS = 7;

    private static final int NO_HORSE = Integer.MIN_VALUE;
    private static final long NO_TICK = Long.MIN_VALUE;

    private boolean active;
    private int activeHorseId = NO_HORSE;
    private long lastForwardPressTick = NO_TICK;
    private int lastForwardPressHorseId = NO_HORSE;
    private boolean previousForwardDown;
    private boolean previousBackDown;
    private boolean previousLeftDown;
    private boolean previousRightDown;

    public Output tick(long tick, boolean eligible, int horseId, boolean forwardDown, boolean backDown,
                       boolean leftDown, boolean rightDown, float forwardImpulse, float leftImpulse) {
        boolean forwardPressed = forwardDown && !previousForwardDown;
        boolean backPressed = backDown && !previousBackDown;
        boolean leftPressed = leftDown && !previousLeftDown;
        boolean rightPressed = rightDown && !previousRightDown;

        if (!eligible) {
            reset();
            return rememberAndPassThrough(forwardDown, backDown, leftDown, rightDown, forwardImpulse, leftImpulse);
        }

        if (tick < lastForwardPressTick) {
            clearPendingForwardPress();
        }

        boolean canceledByManualInput = false;
        if (active && horseId != activeHorseId) {
            deactivate();
        }

        if (active && (forwardPressed || backPressed || leftPressed || rightPressed)) {
            deactivate();
            clearPendingForwardPress();
            canceledByManualInput = true;
        }

        if (!active && !canceledByManualInput) {
            boolean otherMovementDown = backDown || leftDown || rightDown;
            if (otherMovementDown) {
                clearPendingForwardPress();
            } else if (forwardPressed) {
                if (canActivate(tick, horseId)) {
                    activate(horseId);
                    clearPendingForwardPress();
                } else {
                    lastForwardPressTick = tick;
                    lastForwardPressHorseId = horseId;
                }
            } else if (pendingForwardPressExpired(tick)) {
                clearPendingForwardPress();
            }
        }

        Output output = active
                ? Output.autodrive()
                : Output.passThrough(forwardDown, backDown, leftDown, rightDown, forwardImpulse, leftImpulse);
        rememberRawState(forwardDown, backDown, leftDown, rightDown);
        return output;
    }

    public boolean isActive() {
        return active;
    }

    public void reset() {
        deactivate();
        clearPendingForwardPress();
    }

    private boolean canActivate(long tick, int horseId) {
        return lastForwardPressTick != NO_TICK
                && horseId == lastForwardPressHorseId
                && tick - lastForwardPressTick <= DOUBLE_TAP_WINDOW_TICKS;
    }

    private boolean pendingForwardPressExpired(long tick) {
        return lastForwardPressTick != NO_TICK && tick - lastForwardPressTick > DOUBLE_TAP_WINDOW_TICKS;
    }

    private void activate(int horseId) {
        active = true;
        activeHorseId = horseId;
    }

    private void deactivate() {
        active = false;
        activeHorseId = NO_HORSE;
    }

    private void clearPendingForwardPress() {
        lastForwardPressTick = NO_TICK;
        lastForwardPressHorseId = NO_HORSE;
    }

    private Output rememberAndPassThrough(boolean forwardDown, boolean backDown, boolean leftDown, boolean rightDown,
                                          float forwardImpulse, float leftImpulse) {
        rememberRawState(forwardDown, backDown, leftDown, rightDown);
        return Output.passThrough(forwardDown, backDown, leftDown, rightDown, forwardImpulse, leftImpulse);
    }

    private void rememberRawState(boolean forwardDown, boolean backDown, boolean leftDown, boolean rightDown) {
        previousForwardDown = forwardDown;
        previousBackDown = backDown;
        previousLeftDown = leftDown;
        previousRightDown = rightDown;
    }

    public record Output(boolean active, boolean forwardDown, boolean backDown, boolean leftDown, boolean rightDown,
                         float forwardImpulse, float leftImpulse) {
        static Output autodrive() {
            return new Output(true, true, false, false, false, 1.0F, 0.0F);
        }

        static Output passThrough(boolean forwardDown, boolean backDown, boolean leftDown, boolean rightDown,
                                  float forwardImpulse, float leftImpulse) {
            return new Output(false, forwardDown, backDown, leftDown, rightDown, forwardImpulse, leftImpulse);
        }
    }
}
