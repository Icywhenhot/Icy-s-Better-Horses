package icy.betterhorses.net;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static final SoundEvent CALL_WHISTLE = register("call_whistle");
    public static final SoundEvent STABILIZER_INTRO = register("stabilizer_intro");
    public static final SoundEvent STABILIZER_LOOP = register("stabilizer_loop");

    public static final SoundEvent HORSE_ANGRY_SNORT = register("horse_angry_snort");
    public static final SoundEvent HORSE_NEIGH = register("horse_neigh");
    public static final SoundEvent HORSE_SNORT = register("horse_snort");

    public static void init() {
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private ModSounds() {}
}
