package icy.betterhorses.net.network;

import icy.betterhorses.net.tuning.Tuning;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public record TuningPayload(Map<String, String> files) implements CustomPacketPayload {

    public static final Type<TuningPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "tuning"));

    public static final int MAX_FILES = 1024;
    public static final int MAX_NAME = 128;
    public static final int MAX_TEXT = 65536;

    public static TuningPayload current() {
        return new TuningPayload(Tuning.files());
    }

    public static void encode(TuningPayload payload, FriendlyByteBuf buf) {
        Map<String, String> keep = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : payload.files().entrySet()) {
            if (keep.size() >= MAX_FILES) {
                break;
            }
            if (e.getKey().length() <= MAX_NAME && e.getValue().length() <= MAX_TEXT) {
                keep.put(e.getKey(), e.getValue());
            }
        }
        buf.writeVarInt(keep.size());
        keep.forEach((name, json) -> {
            buf.writeUtf(name, MAX_NAME);
            buf.writeUtf(json, MAX_TEXT);
        });
    }

    public static TuningPayload decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        if (n < 0 || n > MAX_FILES) {
            throw new IllegalArgumentException("bad tuning file count " + n);
        }
        Map<String, String> files = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            files.put(buf.readUtf(MAX_NAME), buf.readUtf(MAX_TEXT));
        }
        return new TuningPayload(Map.copyOf(files));
    }

    @Override
    public Type<TuningPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, TuningPayload> {
        @Override
        public TuningPayload decode(FriendlyByteBuf buf) {
            return TuningPayload.decode(buf);
        }

        @Override
        public void encode(FriendlyByteBuf buf, TuningPayload value) {
            TuningPayload.encode(value, buf);
        }
    }
}
