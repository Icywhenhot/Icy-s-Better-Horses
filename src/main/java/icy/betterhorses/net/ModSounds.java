package icy.betterhorses.net;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static final SoundEvent CALL_WHISTLE = sound("call_whistle");
    public static final SoundEvent STABILIZER_INTRO = sound("stabilizer_intro");
    public static final SoundEvent STABILIZER_LOOP = sound("stabilizer_loop");

    public static final SoundEvent HORSE_ANGRY_SNORT = sound("horse_angry_snort");
    public static final SoundEvent HORSE_NEIGH = sound("horse_neigh");
    public static final SoundEvent HORSE_SNORT = sound("horse_snort");
    public static final SoundEvent HORSE_CHARGE_THUD = sound("horse_charge_thud");

    private static SoundEvent sound(String path) {
        ResourceLocation id = new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {}

    private ModSounds() {}
}
