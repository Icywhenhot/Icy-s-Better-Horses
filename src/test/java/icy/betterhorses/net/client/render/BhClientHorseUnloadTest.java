package icy.betterhorses.net.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BhClientHorseUnloadTest {

    private static final int HORSE_ID = 4242;

    @Test
    void handleDropsTheHorseFromEveryPerEntityRenderCache() {
        BhRiderMotion.publish(HORSE_ID, new BhRiderMotion(1, 2, 3, 4, 5));
        assertNotEquals(BhRiderMotion.NONE, BhRiderMotion.get(HORSE_ID), "setup: rider motion should be published");

        BhHorseRenderState.forEntity(HORSE_ID).entityId = HORSE_ID;
        assertEquals(HORSE_ID, BhHorseRenderState.forEntity(HORSE_ID).entityId, "setup: the marked render state should stick");

        BhClientHorseUnload.handle(HORSE_ID);

        assertEquals(BhRiderMotion.NONE, BhRiderMotion.get(HORSE_ID), "rider motion should be gone after unload");
        assertNotEquals(HORSE_ID, BhHorseRenderState.forEntity(HORSE_ID).entityId,
                "the render state entry should be a fresh one after unload, not the marked one");
    }
}
