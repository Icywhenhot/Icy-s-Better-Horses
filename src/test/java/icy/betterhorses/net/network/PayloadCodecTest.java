package icy.betterhorses.net.network;

import icy.betterhorses.net.BhTuning;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayloadCodecTest {

    private static FriendlyByteBuf buf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    @Test
    void horseGearPayloadRoundTrips() {
        HorseGearPayload original = new HorseGearPayload(1234, 3, 2);
        FriendlyByteBuf buf = buf();
        HorseGearPayload.encode(original, buf);
        assertEquals(original, HorseGearPayload.decode(buf));
        assertEquals(0, buf.readableBytes(), "decode must consume every written byte");
    }

    @Test
    void radialCommandPayloadRoundTrips() {
        RadialCommandPayload original = new RadialCommandPayload(99, "icys_better_horses:wander", "icys_better_horses:top_end");
        FriendlyByteBuf buf = buf();
        RadialCommandPayload.encode(original, buf);
        assertEquals(original, RadialCommandPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void bhFreeLookPayloadRoundTripsWhenTrue() {
        BhFreeLookPayload original = new BhFreeLookPayload(7, true);
        FriendlyByteBuf buf = buf();
        BhFreeLookPayload.encode(original, buf);
        assertEquals(original, BhFreeLookPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void bhFreeLookPayloadRoundTripsWhenFalse() {
        BhFreeLookPayload original = new BhFreeLookPayload(55, false);
        FriendlyByteBuf buf = buf();
        BhFreeLookPayload.encode(original, buf);
        assertEquals(original, BhFreeLookPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void bhRearPayloadRoundTrips() {
        BhRearPayload original = new BhRearPayload(42);
        FriendlyByteBuf buf = buf();
        BhRearPayload.encode(original, buf);
        assertEquals(original, BhRearPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void cartSizePayloadRoundTrips() {
        CartSizePayload original = new CartSizePayload(8080);
        FriendlyByteBuf buf = buf();
        CartSizePayload.encode(original, buf);
        assertEquals(original, CartSizePayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void cartSizePayloadRoundTripsWithNegativeValue() {
        CartSizePayload original = new CartSizePayload(-12345);
        FriendlyByteBuf buf = buf();
        CartSizePayload.encode(original, buf);
        assertEquals(original, CartSizePayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseManagePayloadRoundTrips() {
        HorseManagePayload original =
                new HorseManagePayload(UUID.fromString("deadbeef-0000-1111-2222-333344445555"), 7);
        FriendlyByteBuf buf = buf();
        HorseManagePayload.encode(original, buf);
        assertEquals(original, HorseManagePayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void callHorsePayloadRoundTrips() {
        CallHorsePayload original = new CallHorsePayload();
        FriendlyByteBuf buf = buf();
        CallHorsePayload.encode(original, buf);
        assertEquals(original, CallHorsePayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseRecallPayloadRoundTrips() {
        HorseRecallPayload original = new HorseRecallPayload();
        FriendlyByteBuf buf = buf();
        HorseRecallPayload.encode(original, buf);
        assertEquals(original, HorseRecallPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void openHorseRosterPayloadRoundTrips() {
        OpenHorseRosterPayload original = new OpenHorseRosterPayload();
        FriendlyByteBuf buf = buf();
        OpenHorseRosterPayload.encode(original, buf);
        assertEquals(original, OpenHorseRosterPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseChargeShakePayloadRoundTrips() {
        HorseChargeShakePayload original = new HorseChargeShakePayload();
        FriendlyByteBuf buf = buf();
        HorseChargeShakePayload.encode(original, buf);
        assertEquals(original, HorseChargeShakePayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseManageResultPayloadRoundTripsOnSuccess() {
        HorseManageResultPayload original = new HorseManageResultPayload(
                UUID.fromString("cafebabe-0000-1111-2222-666677778888"),
                3,
                true,
                "bh.manage.success.stable");
        FriendlyByteBuf buf = buf();
        HorseManageResultPayload.encode(original, buf);
        assertEquals(original, HorseManageResultPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseManageResultPayloadRoundTripsOnFailure() {
        HorseManageResultPayload original = new HorseManageResultPayload(
                UUID.fromString("facefeed-0000-1111-2222-999900001111"),
                9,
                false,
                "bh.manage.error.notrusted");
        FriendlyByteBuf buf = buf();
        HorseManageResultPayload.encode(original, buf);
        assertEquals(original, HorseManageResultPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void trustSyncPayloadRoundTripsWithEntries() {
        TrustSyncPayload original = new TrustSyncPayload(List.of(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
                UUID.fromString("11111111-2222-3333-4444-555555555555")));
        FriendlyByteBuf buf = buf();
        TrustSyncPayload.encode(original, buf);
        assertEquals(original, TrustSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void trustSyncPayloadRoundTripsWhenEmpty() {
        TrustSyncPayload original = new TrustSyncPayload(List.of());
        FriendlyByteBuf buf = buf();
        TrustSyncPayload.encode(original, buf);
        assertEquals(original, TrustSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseRosterSyncPayloadRoundTripsWithEntries() {
        HorseRosterEntry populated = new HorseRosterEntry(
                UUID.fromString("11111111-0000-1111-2222-444455556666"),
                "Shadowfax",
                "arabian",
                "icys_better_horses:female",
                true,
                250,
                true,
                false,
                true,
                "minecraft:overworld",
                new BlockPos(100, 64, -200),
                "minecraft:horse",
                3,
                2,
                false,
                5);
        HorseRosterEntry sentinel = new HorseRosterEntry(
                UUID.fromString("22222222-0000-1111-2222-777788889999"),
                "",
                "clydesdale",
                "icys_better_horses:male",
                false,
                0,
                false,
                true,
                true,
                "minecraft:the_nether",
                new BlockPos(-50, 10, 300),
                "minecraft:donkey",
                -1,
                -1,
                true,
                -1);
        HorseRosterEntry mixed = new HorseRosterEntry(
                UUID.fromString("33333333-0000-1111-2222-101112131415"),
                "Bucephalus",
                "mustang",
                "",
                true,
                999,
                false,
                true,
                false,
                "minecraft:the_end",
                new BlockPos(7, 200, -77),
                "minecraft:mule",
                8,
                6,
                false,
                12);

        HorseRosterSyncPayload original = new HorseRosterSyncPayload(List.of(populated, sentinel, mixed));
        FriendlyByteBuf buf = buf();
        HorseRosterSyncPayload.encode(original, buf);
        assertEquals(original, HorseRosterSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void horseRosterSyncPayloadRoundTripsWhenEmpty() {
        HorseRosterSyncPayload original = new HorseRosterSyncPayload(List.of());
        FriendlyByteBuf buf = buf();
        HorseRosterSyncPayload.encode(original, buf);
        assertEquals(original, HorseRosterSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void configSyncPayloadRoundTripsWithClassAbilitiesEnabled() {
        ConfigSyncPayload original = new ConfigSyncPayload(
                List.of("saddle_speed", "auto_feed"),
                true,
                false,
                List.of("charge_jump"),
                new BhTuning(42, 17, 250, 3, 9, 0.25D));
        FriendlyByteBuf buf = buf();
        ConfigSyncPayload.encode(original, buf);
        assertEquals(original, ConfigSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void configSyncPayloadRoundTripsWithBreedAbilitiesEnabled() {
        ConfigSyncPayload original = new ConfigSyncPayload(
                List.of("fast_travel"),
                false,
                true,
                List.of("night_vision", "double_jump", "swim_boost"),
                new BhTuning(63, 200, 777, 5, 20, 0.75D));
        FriendlyByteBuf buf = buf();
        ConfigSyncPayload.encode(original, buf);
        assertEquals(original, ConfigSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void configSyncPayloadRoundTripsWhenListsEmpty() {
        ConfigSyncPayload original = new ConfigSyncPayload(
                List.of(),
                true,
                true,
                List.of(),
                new BhTuning(5, 30, 100, 2, 4, 0.5D));
        FriendlyByteBuf buf = buf();
        ConfigSyncPayload.encode(original, buf);
        assertEquals(original, ConfigSyncPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void configSyncPayloadTruncatesDisabledFeaturesOverMaxKeysCap() {
        List<String> tooManyFeatures = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            tooManyFeatures.add("feature-" + i);
        }
        ConfigSyncPayload original = new ConfigSyncPayload(
                tooManyFeatures,
                true,
                false,
                List.of("ability-a", "ability-b"),
                new BhTuning(11, 22, 33, 4, 8, 0.125D));
        FriendlyByteBuf buf = buf();
        ConfigSyncPayload.encode(original, buf);
        ConfigSyncPayload decoded = ConfigSyncPayload.decode(buf);

        assertEquals(256, decoded.disabledFeatures().size(), "encode must cap at MAX_KEYS = 256");
        assertEquals(tooManyFeatures.subList(0, 256), decoded.disabledFeatures());
        assertEquals(List.of("ability-a", "ability-b"), decoded.disabledAbilities());
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void breedDataPayloadRoundTripsWithEntries() {
        BreedDataPayload original = new BreedDataPayload(List.of(
                new BreedDataPayload.Entry("arabian", "fast", 2, 4, 15),
                new BreedDataPayload.Entry("clydesdale", "draft", 5, 1, 30)));
        FriendlyByteBuf buf = buf();
        BreedDataPayload.encode(original, buf);
        assertEquals(original, BreedDataPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void breedDataPayloadRoundTripsWhenEmpty() {
        BreedDataPayload original = new BreedDataPayload(List.of());
        FriendlyByteBuf buf = buf();
        BreedDataPayload.encode(original, buf);
        assertEquals(original, BreedDataPayload.decode(buf));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void breedDataPayloadTruncatesEntriesOverMaxEntriesCap() {
        List<BreedDataPayload.Entry> tooManyEntries = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            tooManyEntries.add(new BreedDataPayload.Entry("breed-" + i, "archetype-" + i, i, i + 1, i + 2));
        }
        BreedDataPayload original = new BreedDataPayload(tooManyEntries);
        FriendlyByteBuf buf = buf();
        BreedDataPayload.encode(original, buf);
        BreedDataPayload decoded = BreedDataPayload.decode(buf);

        assertEquals(128, decoded.entries().size(), "encode must cap at MAX_ENTRIES = 128");
        assertEquals(tooManyEntries.subList(0, 128), decoded.entries());
        assertEquals(0, buf.readableBytes());
    }
}
