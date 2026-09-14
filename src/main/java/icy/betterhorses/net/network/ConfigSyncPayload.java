package icy.betterhorses.net.network;

import icy.betterhorses.net.BhTuning;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ConfigSyncPayload(
        List<String> disabledFeatures,
        boolean classAbilities,
        boolean breedAbilities,
        List<String> disabledAbilities,
        BhTuning tuning) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "config_sync"));

    private static final int MAX_KEYS = 256;
    private static final int MAX_KEY_LENGTH = 64;

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ConfigSyncPayload> {
        @Override
        public ConfigSyncPayload decode(FriendlyByteBuf buf) {
            List<String> off = readKeys(buf);
            boolean classOn = buf.readBoolean();
            boolean breedOn = buf.readBoolean();
            List<String> offAbilities = readKeys(buf);
            BhTuning tuning = new BhTuning(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readFloat());
            return new ConfigSyncPayload(off, classOn, breedOn, offAbilities, tuning.clamped());
        }

        @Override
        public void encode(FriendlyByteBuf buf, ConfigSyncPayload value) {
            writeKeys(buf, value.disabledFeatures());
            buf.writeBoolean(value.classAbilities());
            buf.writeBoolean(value.breedAbilities());
            writeKeys(buf, value.disabledAbilities());
            BhTuning tuning = value.tuning();
            buf.writeVarInt(tuning.bondAmount());
            buf.writeVarInt(tuning.bondMinutes());
            buf.writeVarInt(tuning.spawnWeight());
            buf.writeVarInt(tuning.groupMin());
            buf.writeVarInt(tuning.groupMax());
            buf.writeFloat((float) tuning.spawnFloor());
        }

        private static List<String> readKeys(FriendlyByteBuf buf) {
            int count = Math.min(buf.readVarInt(), MAX_KEYS);
            List<String> keys = new java.util.ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                keys.add(buf.readUtf(MAX_KEY_LENGTH));
            }
            return List.copyOf(keys);
        }

        private static void writeKeys(FriendlyByteBuf buf, List<String> keys) {
            int count = Math.min(keys.size(), MAX_KEYS);
            buf.writeVarInt(count);
            for (int i = 0; i < count; i++) {
                buf.writeUtf(keys.get(i), MAX_KEY_LENGTH);
            }
        }
    }
}
