package icy.betterhorses.net.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.Arrays;
import java.util.Optional;

public final class BhJumpClips {

    static final int HOLD = 0;
    static final int TAKEOFF = 1;
    static final int RISE = 2;
    static final int APEX = 3;
    static final int FALL = 4;

    static final int BODY = 0;
    static final int NECK = 1;
    static final int HEAD = 2;
    static final int LEFT_EAR = 3;
    static final int RIGHT_EAR = 4;
    static final int TAIL = 5;
    static final int FRONT_LEFT = 6;
    static final int FRONT_RIGHT = 7;
    static final int BACK_LEFT = 8;
    static final int BACK_RIGHT = 9;

    static final int BONES = 10;
    static final int POSE_SIZE = BONES * 6;

    private static final String[] CLIP_NAMES = {"hold", "takeoff", "rise", "apex", "fall"};
    private static final String[] BONE_NAMES = {"body2", "neck", "head", "left_ear", "right_ear", "tail",
            "front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg"};

    private static final ResourceLocation FILE =
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "clips/horse_jump.json");

    private static final byte LINEAR = 0;
    private static final byte SMOOTH = 1;
    private static final byte STEP = 2;

    private static final float[] PITCH = new float[3];

    private static Clip[] clips;
    private static float crouchEnd = 0.20833F;
    private static float apexAt = 0.3F;

    private static final class Track {
        final float[] times;
        final float[] values;
        final byte[] modes;

        Track(int n) {
            times = new float[n];
            values = new float[n * 3];
            modes = new byte[n];
        }
    }

    private static final class Clip {
        float length;
        boolean loop;
        final Track[] tracks = new Track[BONES * 2];
    }

    private BhJumpClips() {}

    static boolean ready() {
        return clips != null;
    }

    static float crouchEnd() {
        return crouchEnd;
    }

    static float apexAt() {
        return apexAt;
    }

    static float length(int clip) {
        return clips == null ? 0.0F : clips[clip].length;
    }

    static void sample(int clip, float time, float[] out) {
        if (clips == null) {
            Arrays.fill(out, 0.0F);
            return;
        }
        Clip c = clips[clip];
        float t = c.loop && c.length > 0.0F ? time % c.length : time;
        if (t < 0.0F) {
            t += c.length;
        }
        for (int bone = 0; bone < BONES; bone++) {
            read(c.tracks[bone * 2], t, out, bone * 6);
            read(c.tracks[bone * 2 + 1], t, out, bone * 6 + 3);
        }
    }

    static float bodyPitch(int clip, float time) {
        if (clips == null) {
            return 0.0F;
        }
        Clip c = clips[clip];
        Track track = c.tracks[BODY * 2];
        if (track == null) {
            return 0.0F;
        }
        float t = c.loop && c.length > 0.0F ? time % c.length : time;
        read(track, t, PITCH, 0);
        return PITCH[0];
    }

    static void mix(float[] into, float[] other, float k) {
        for (int i = 0; i < into.length; i++) {
            into[i] += (other[i] - into[i]) * k;
        }
    }

    private static void read(Track track, float t, float[] out, int at) {
        if (track == null) {
            out[at] = 0.0F;
            out[at + 1] = 0.0F;
            out[at + 2] = 0.0F;
            return;
        }
        int n = track.times.length;
        if (n == 1 || t <= track.times[0]) {
            copy(track, 0, out, at);
            return;
        }
        if (t >= track.times[n - 1]) {
            copy(track, n - 1, out, at);
            return;
        }
        int i = 0;
        while (i < n - 2 && t > track.times[i + 1]) {
            i++;
        }
        if (track.modes[i] == STEP) {
            copy(track, i, out, at);
            return;
        }
        float span = track.times[i + 1] - track.times[i];
        float u = span > 0.0F ? (t - track.times[i]) / span : 0.0F;
        boolean smooth = track.modes[i] == SMOOTH || track.modes[i + 1] == SMOOTH;
        for (int axis = 0; axis < 3; axis++) {
            float p1 = track.values[i * 3 + axis];
            float p2 = track.values[(i + 1) * 3 + axis];
            if (!smooth) {
                out[at + axis] = p1 + (p2 - p1) * u;
                continue;
            }
            float p0 = track.values[Math.max(0, i - 1) * 3 + axis];
            float p3 = track.values[Math.min(n - 1, i + 2) * 3 + axis];
            float u2 = u * u;
            float u3 = u2 * u;
            out[at + axis] = 0.5F * (2.0F * p1 + (p2 - p0) * u
                    + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * u2
                    + (3.0F * p1 - p0 - 3.0F * p2 + p3) * u3);
        }
    }

    private static void copy(Track track, int key, float[] out, int at) {
        out[at] = track.values[key * 3];
        out[at + 1] = track.values[key * 3 + 1];
        out[at + 2] = track.values[key * 3 + 2];
    }

    public static void load(ResourceManager manager) {
        Optional<Resource> res = manager.getResource(FILE);
        if (res.isEmpty()) {
            clips = null;
            IcysBetterHorses.LOGGER.warn("[jump] {} missing, jump clips disabled", FILE);
            return;
        }
        try (Reader reader = res.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject all = root.getAsJsonObject("clips");
            Clip[] parsed = new Clip[CLIP_NAMES.length];
            for (int i = 0; i < CLIP_NAMES.length; i++) {
                JsonObject obj = all.getAsJsonObject(CLIP_NAMES[i]);
                if (obj == null) {
                    throw new IllegalStateException("no " + CLIP_NAMES[i] + " clip");
                }
                parsed[i] = parse(obj);
            }
            crouchEnd = root.has("crouch_end") ? root.get("crouch_end").getAsFloat() : 0.20833F;
            apexAt = root.has("apex_at") ? root.get("apex_at").getAsFloat() : parsed[APEX].length * 0.6F;
            clips = parsed;
        } catch (Exception e) {
            clips = null;
            IcysBetterHorses.LOGGER.warn("[jump] could not read {}", FILE, e);
        }
    }

    private static Clip parse(JsonObject obj) {
        Clip clip = new Clip();
        clip.length = obj.has("length") ? obj.get("length").getAsFloat() : 0.0F;
        clip.loop = obj.has("loop") && obj.get("loop").getAsBoolean();
        JsonObject bones = obj.getAsJsonObject("bones");
        if (bones == null) {
            return clip;
        }
        for (int bone = 0; bone < BONES; bone++) {
            JsonObject chans = bones.getAsJsonObject(BONE_NAMES[bone]);
            if (chans == null) {
                continue;
            }
            clip.tracks[bone * 2] = track(chans.getAsJsonArray("rotation"));
            clip.tracks[bone * 2 + 1] = track(chans.getAsJsonArray("position"));
        }
        return clip;
    }

    private static Track track(JsonArray keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        Track track = new Track(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            JsonArray key = keys.get(i).getAsJsonArray();
            track.times[i] = key.get(0).getAsFloat();
            track.values[i * 3] = key.get(1).getAsFloat();
            track.values[i * 3 + 1] = key.get(2).getAsFloat();
            track.values[i * 3 + 2] = key.get(3).getAsFloat();
            JsonElement mode = key.size() > 4 ? key.get(4) : null;
            String name = mode == null ? "linear" : mode.getAsString();
            track.modes[i] = switch (name) {
                case "catmullrom" -> SMOOTH;
                case "step" -> STEP;
                default -> LINEAR;
            };
        }
        return track;
    }
}
