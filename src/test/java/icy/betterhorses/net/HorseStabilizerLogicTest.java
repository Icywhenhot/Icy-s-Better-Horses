package icy.betterhorses.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorseStabilizerLogicTest {
    @Test
    void staysClosedForSmallFalls() {
        HorseStabilizerState state = HorseStabilizerLogic.computeState(
                true,
                false,
                false,
                false,
                false,
                -0.4D,
                1.5F,
                HorseStabilizerState.CLOSED);

        assertEquals(HorseStabilizerState.CLOSED, state);
        assertEquals(
                HorseStabilizerState.CLOSED,
                HorseStabilizerLogic.resolveLandingState(true, 1.5F, HorseStabilizerState.CLOSED));
    }

    @Test
    void halfOpensAtModerateFallsWithoutJumpingStraightToOpen() {
        HorseStabilizerState state = HorseStabilizerLogic.computeState(
                true,
                false,
                false,
                false,
                false,
                -0.35D,
                HorseStabilizerLogic.HALF_OPEN_FALL_DISTANCE,
                HorseStabilizerState.CLOSED);

        assertEquals(HorseStabilizerState.HALF_OPEN, state);
    }

    @Test
    void opensOnlyForLargeFalls() {
        HorseStabilizerState state = HorseStabilizerLogic.computeState(
                true,
                false,
                false,
                false,
                false,
                -0.45D,
                HorseStabilizerLogic.OPEN_FALL_DISTANCE,
                HorseStabilizerState.HALF_OPEN);

        assertEquals(HorseStabilizerState.OPEN, state);
    }

    @Test
    void openStateSticksUntilGrounded() {
        HorseStabilizerState midairState = HorseStabilizerLogic.computeState(
                true,
                false,
                false,
                false,
                false,
                -0.01D,
                0.0F,
                HorseStabilizerState.OPEN);
        HorseStabilizerState groundedState = HorseStabilizerLogic.computeState(
                true,
                true,
                false,
                false,
                false,
                0.0D,
                0.0F,
                HorseStabilizerState.OPEN);

        assertEquals(HorseStabilizerState.OPEN, midairState);
        assertEquals(HorseStabilizerState.CLOSED, groundedState);
    }

    @Test
    void soundOnlyPlaysForFullyOpenState() {
        assertFalse(HorseStabilizerLogic.shouldPlaySteam(HorseStabilizerState.CLOSED));
        assertFalse(HorseStabilizerLogic.shouldPlaySteam(HorseStabilizerState.HALF_OPEN));
        assertTrue(HorseStabilizerLogic.shouldPlaySteam(HorseStabilizerState.OPEN));
    }

    @Test
    void landingStateHonorsCurrentActiveDeployment() {
        assertEquals(
                HorseStabilizerState.HALF_OPEN,
                HorseStabilizerLogic.resolveLandingState(true, 0.5F, HorseStabilizerState.HALF_OPEN));
        assertEquals(
                HorseStabilizerState.OPEN,
                HorseStabilizerLogic.resolveLandingState(true, 0.5F, HorseStabilizerState.OPEN));
    }
}
