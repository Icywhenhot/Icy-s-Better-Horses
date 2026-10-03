package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModSounds {

    private static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<>();

    public static final SoundEvent CALL_WHISTLE = register("call_whistle");
    public static final SoundEvent STABILIZER_INTRO = register("stabilizer_intro");
    public static final SoundEvent STABILIZER_LOOP = register("stabilizer_loop");

    public static final SoundEvent HORSE_ANGRY_SNORT = register("horse_angry_snort");
    public static final SoundEvent HORSE_NEIGH = register("horse_neigh");
    public static final SoundEvent HORSE_SNORT = register("horse_snort");
    public static final SoundEvent HORSE_CHARGE_THUD = register("horse_charge_thud");

    public static void init() {
        SOUNDS.forEach((path, value) -> Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path), value));
    }

    private static SoundEvent register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(id);
        SOUNDS.put(path, sound);
        return sound;
    }

    private ModSounds() {}
}
