package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<SoundEvent> CALL_WHISTLE =
            SOUNDS.register("call_whistle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "call_whistle")));
    public static final RegistryObject<SoundEvent> STABILIZER_INTRO =
            SOUNDS.register("stabilizer_intro", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stabilizer_intro")));
    public static final RegistryObject<SoundEvent> STABILIZER_LOOP =
            SOUNDS.register("stabilizer_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stabilizer_loop")));

    public static final RegistryObject<SoundEvent> HORSE_ANGRY_SNORT = horseSound("horse_angry_snort");
    public static final RegistryObject<SoundEvent> HORSE_NEIGH = horseSound("horse_neigh");
    public static final RegistryObject<SoundEvent> HORSE_SNORT = horseSound("horse_snort");
    public static final RegistryObject<SoundEvent> HORSE_CHARGE_THUD = horseSound("horse_charge_thud");

    private static RegistryObject<SoundEvent> horseSound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path)));
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    private ModSounds() {}
}
