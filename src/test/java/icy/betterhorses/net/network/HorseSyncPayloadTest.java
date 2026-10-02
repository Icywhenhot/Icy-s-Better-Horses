package icy.betterhorses.net.network;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HorseSyncPayloadTest {
    private static final UUID ID = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");

    @Test
    void rosterKeepsTheWireFormat() {
        String bytes = "0100112233445566778899aabbccddeeff07c389636c6169720a6164646f6e3a706f6e79"
                + "17696379732d6265747465722d686f727365733a6d617265019601000101136d696e656372616674"
                + "3a6f766572776f726c6400000000000000000a6164646f6e3a706f6e7900040100";
        var payload = new HorseRosterSyncPayload(List.of(new HorseRosterEntry(ID, "Éclair", "addon:pony",
                "icys-better-horses:mare", true, 150, false, true, true, "minecraft:overworld",
                BlockPos.ZERO, "addon:pony", -1, 3, true, -1)));
        var codec = new HorseRosterSyncPayload.StreamCodec();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(HexFormat.of().parseHex(bytes)));
        try {
            assertEquals(payload, codec.decode(buf));
            assertEquals(0, buf.readableBytes());
            buf.clear();
            codec.encode(buf, payload);
            assertEquals(bytes, ByteBufUtil.hexDump(buf));
        } finally {
            buf.release();
        }
    }

    @Test
    void trustKeepsUuidOrderAndWireFormat() {
        String bytes = "0200112233445566778899aabbccddeeffffeeddccbbaa99887766554433221100";
        var payload = new TrustSyncPayload(List.of(ID, UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100")));
        var codec = new TrustSyncPayload.StreamCodec();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(HexFormat.of().parseHex(bytes)));
        try {
            assertEquals(payload, codec.decode(buf));
            assertEquals(0, buf.readableBytes());
            buf.clear();
            codec.encode(buf, payload);
            assertEquals(bytes, ByteBufUtil.hexDump(buf));
        } finally {
            buf.release();
        }
    }

    @Test
    void emptySyncListsUseOneZeroByte() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var roster = new HorseRosterSyncPayload.StreamCodec();
            roster.encode(buf, new HorseRosterSyncPayload(List.of()));
            assertEquals("00", ByteBufUtil.hexDump(buf));
            assertEquals(List.of(), roster.decode(buf).entries());
            assertEquals(0, buf.readableBytes());
            buf.clear();
            var trust = new TrustSyncPayload.StreamCodec();
            trust.encode(buf, new TrustSyncPayload(List.of()));
            assertEquals("00", ByteBufUtil.hexDump(buf));
            assertEquals(List.of(), trust.decode(buf).trustingOwners());
            assertEquals(0, buf.readableBytes());
        } finally {
            buf.release();
        }
    }

    @Test
    void trustListCanCrossTheVarIntBoundary() {
        var payload = new TrustSyncPayload(Collections.nCopies(128, ID));
        var codec = new TrustSyncPayload.StreamCodec();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            codec.encode(buf, payload);
            assertEquals("8001", ByteBufUtil.hexDump(buf, 0, 2));
            assertEquals(payload, codec.decode(buf));
            assertEquals(0, buf.readableBytes());
        } finally {
            buf.release();
        }
    }

    @Test
    void truncatedListsFailInsteadOfReturningPartialSync() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(new byte[]{1}));
        try {
            assertThrows(IndexOutOfBoundsException.class, () -> new HorseRosterSyncPayload.StreamCodec().decode(buf));
            buf.readerIndex(0);
            assertThrows(IndexOutOfBoundsException.class, () -> new TrustSyncPayload.StreamCodec().decode(buf));
        } finally {
            buf.release();
        }
    }
}
