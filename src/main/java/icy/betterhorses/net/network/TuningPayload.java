package icy.betterhorses.net.network;

import icy.betterhorses.net.tuning.Tuning;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;

public record TuningPayload(Map<String, String> files) {

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
}
