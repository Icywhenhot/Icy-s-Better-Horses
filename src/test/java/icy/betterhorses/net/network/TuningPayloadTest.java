package icy.betterhorses.net.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TuningPayloadTest {

    private static TuningPayload roundTrip(TuningPayload in) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            TuningPayload.encode(in, buf);
            TuningPayload out = TuningPayload.decode(buf);
            assertEquals(0, buf.readableBytes());
            return out;
        } finally {
            buf.release();
        }
    }

    @Test
    void roundTripKeepsEveryFile() {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("bond", "{\"max_bond\": 200}");
        files.put("class/race", "{\"max_speed\": 16.5}");
        files.put("ability/top_end", "{\"speed_bonus\": [0.4, 0.6]}");
        files.put("breeding", "{\"inherit\": \"average\", \"note\": \"éè ☃\"}");
        assertEquals(files, roundTrip(new TuningPayload(files)).files());
    }

    @Test
    void emptyIsFine() {
        assertTrue(roundTrip(new TuningPayload(Map.of())).files().isEmpty());
    }

    @Test
    void oversizedFilesAreLeftOutNotCorrupted() {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("bond", "{}");
        files.put("ability/huge", "{\"a\": \"" + "x".repeat(TuningPayload.MAX_TEXT) + "\"}");
        files.put("x".repeat(TuningPayload.MAX_NAME + 1), "{}");
        Map<String, String> got = roundTrip(new TuningPayload(files)).files();
        assertEquals(Map.of("bond", "{}"), got);
    }

    @Test
    void badCountIsRejected() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeVarInt(TuningPayload.MAX_FILES + 1);
            assertThrows(IllegalArgumentException.class, () -> TuningPayload.decode(buf));
        } finally {
            buf.release();
        }
    }
}
