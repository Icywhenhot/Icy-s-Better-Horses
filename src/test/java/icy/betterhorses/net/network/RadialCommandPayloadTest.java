package icy.betterhorses.net.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RadialCommandPayloadTest {
    @Test
    void roundTripKeepsTheSelectedAbility() {
        RadialCommandPayload payload = new RadialCommandPayload(42, "icys_better_horses:ability", "addon:second");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RadialCommandPayload.encode(payload, buf);
            assertEquals(payload, RadialCommandPayload.decode(buf));
            assertEquals(0, buf.readableBytes());
        } finally {
            buf.release();
        }
    }

    @Test
    void ordinaryCommandHasNoAbilitySelection() {
        assertEquals("", new RadialCommandPayload(42, "addon:feed").abilityId());
    }
}
