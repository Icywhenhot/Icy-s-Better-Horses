package icy.betterhorses.net.network;

import icy.betterhorses.net.BhTuning;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public record ConfigSyncPayload(
        List<String> disabledFeatures,
        boolean classAbilities,
        boolean breedAbilities,
        List<String> disabledAbilities,
        BhTuning tuning,
        List<String> breedingFoods) {


    private static final int MAX_KEYS = 256;
    private static final int MAX_KEY_LENGTH = 64;
    private static final int MAX_ID_LENGTH = 256;


    public static void encode(ConfigSyncPayload payload, FriendlyByteBuf buf) {
        writeKeys(buf, payload.disabledFeatures(), MAX_KEY_LENGTH);
        buf.writeBoolean(payload.classAbilities());
        buf.writeBoolean(payload.breedAbilities());
        writeKeys(buf, payload.disabledAbilities(), MAX_KEY_LENGTH);
        BhTuning tuning = payload.tuning();
        buf.writeVarInt(tuning.bondAmount());
        buf.writeVarInt(tuning.bondMinutes());
        buf.writeVarInt(tuning.spawnWeight());
        buf.writeVarInt(tuning.groupMin());
        buf.writeVarInt(tuning.groupMax());
        buf.writeFloat((float) tuning.spawnFloor());
        buf.writeVarInt(tuning.reviveLevels());
        buf.writeVarInt(tuning.reviveBondLoss());
        buf.writeVarInt(tuning.whistleRange());
        writeKeys(buf, payload.breedingFoods(), MAX_ID_LENGTH);
    }

    public static ConfigSyncPayload decode(FriendlyByteBuf buf) {
        List<String> off = readKeys(buf, MAX_KEY_LENGTH);
        boolean classOn = buf.readBoolean();
        boolean breedOn = buf.readBoolean();
        List<String> offAbilities = readKeys(buf, MAX_KEY_LENGTH);
        BhTuning tuning = new BhTuning(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt());
        List<String> foods = readKeys(buf, MAX_ID_LENGTH);
        return new ConfigSyncPayload(off, classOn, breedOn, offAbilities, tuning.clamped(), foods);
    }

    private static List<String> readKeys(FriendlyByteBuf buf, int maxLength) {
        int count = Math.min(buf.readVarInt(), MAX_KEYS);
        List<String> keys = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add(buf.readUtf(maxLength));
        }
        return List.copyOf(keys);
    }

    private static void writeKeys(FriendlyByteBuf buf, List<String> keys, int maxLength) {
        int count = Math.min(keys.size(), MAX_KEYS);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeUtf(keys.get(i), maxLength);
        }
    }
}
