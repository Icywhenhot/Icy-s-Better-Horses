package icy.betterhorses.net.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorseAutodriveControllerTest {
    private static final int HORSE_ID = 12;
    private static final int OTHER_HORSE_ID = 27;

    @Test
    void activatesOnSecondForwardTapWithinWindow() {
        HorseAutodriveController controller = new HorseAutodriveController();

        tick(controller, 1, HORSE_ID, true, false, false, false);
        tick(controller, 2, HORSE_ID, false, false, false, false);
        HorseAutodriveController.Output output = tick(controller, 4, HORSE_ID, true, false, false, false);

        assertTrue(controller.isActive());
        assertTrue(output.active());
        assertTrue(output.forwardDown());
        assertFalse(output.backDown());
        assertFalse(output.leftDown());
        assertFalse(output.rightDown());
        assertTrue(output.forwardImpulse() > 0.99F);
    }

    @Test
    void holdingForwardDoesNotCountAsDoubleTap() {
        HorseAutodriveController controller = new HorseAutodriveController();

        tick(controller, 1, HORSE_ID, true, false, false, false);
        tick(controller, 2, HORSE_ID, true, false, false, false);
        HorseAutodriveController.Output output = tick(controller, 3, HORSE_ID, true, false, false, false);

        assertFalse(controller.isActive());
        assertFalse(output.active());
    }

    @Test
    void secondTapAfterWindowStartsOverInsteadOfActivating() {
        HorseAutodriveController controller = new HorseAutodriveController();

        tick(controller, 1, HORSE_ID, true, false, false, false);
        tick(controller, 2, HORSE_ID, false, false, false, false);
        HorseAutodriveController.Output output = tick(
                controller,
                1 + HorseAutodriveController.DOUBLE_TAP_WINDOW_TICKS + 2L,
                HORSE_ID,
                true,
                false,
                false,
                false
        );

        assertFalse(controller.isActive());
        assertFalse(output.active());
    }

    @Test
    void manualMovementCancelsAutodriveAndKeepsThatInput() {
        HorseAutodriveController controller = new HorseAutodriveController();

        activate(controller);
        HorseAutodriveController.Output output = tick(controller, 6, HORSE_ID, false, false, true, false);

        assertFalse(controller.isActive());
        assertFalse(output.active());
        assertTrue(output.leftDown());
        assertFalse(output.forwardDown());
    }

    @Test
    void forwardPressCancelsAutodriveWithoutInstantlyRearming() {
        HorseAutodriveController controller = new HorseAutodriveController();

        activate(controller);
        HorseAutodriveController.Output cancelOutput = tick(controller, 6, HORSE_ID, true, false, false, false);
        HorseAutodriveController.Output laterOutput = tick(controller, 8, HORSE_ID, true, false, false, false);

        assertFalse(controller.isActive());
        assertFalse(cancelOutput.active());
        assertTrue(cancelOutput.forwardDown());
        assertFalse(laterOutput.active());
    }

    @Test
    void losingHorseControlResetsAutodriveState() {
        HorseAutodriveController controller = new HorseAutodriveController();

        activate(controller);
        HorseAutodriveController.Output output = controller.tick(6, false, 0, false, false, false, false, 0.0F, 0.0F);

        assertFalse(controller.isActive());
        assertFalse(output.active());
    }

    @Test
    void secondTapOnDifferentHorseDoesNotActivate() {
        HorseAutodriveController controller = new HorseAutodriveController();

        tick(controller, 1, HORSE_ID, true, false, false, false);
        tick(controller, 2, HORSE_ID, false, false, false, false);
        HorseAutodriveController.Output output = tick(controller, 4, OTHER_HORSE_ID, true, false, false, false);

        assertFalse(controller.isActive());
        assertFalse(output.active());
    }

    private static void activate(HorseAutodriveController controller) {
        tick(controller, 1, HORSE_ID, true, false, false, false);
        tick(controller, 2, HORSE_ID, false, false, false, false);
        tick(controller, 4, HORSE_ID, true, false, false, false);
        tick(controller, 5, HORSE_ID, false, false, false, false);
        assertTrue(controller.isActive());
    }

    private static HorseAutodriveController.Output tick(HorseAutodriveController controller, long tick, int horseId,
                                                        boolean forward, boolean back, boolean left, boolean right) {
        return controller.tick(
                tick,
                true,
                horseId,
                forward,
                back,
                left,
                right,
                impulse(forward, back),
                impulse(left, right)
        );
    }

    private static float impulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        }
        return positive ? 1.0F : -1.0F;
    }
}
