package icy.betterhorses.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BondTierTest {

    @Test
    void tierBoundaries() {
        assertEquals(0, BhHorseTraits.bondTier(0));
        assertEquals(0, BhHorseTraits.bondTier(39));
        assertEquals(1, BhHorseTraits.bondTier(40));
        assertEquals(1, BhHorseTraits.bondTier(99));
        assertEquals(2, BhHorseTraits.bondTier(100));
        assertEquals(0, BhHorseTraits.bondTier(-5));
    }
}
