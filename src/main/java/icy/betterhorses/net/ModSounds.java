package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<SoundEvent> CALL_WHISTLE =
            SOUNDS.register("call_whistle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.MOD_ID, "call_whistle")));
    public static final RegistryObject<SoundEvent> STABILIZER_INTRO =
            SOUNDS.register("stabilizer_intro", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.MOD_ID, "stabilizer_intro")));
    public static final RegistryObject<SoundEvent> STABILIZER_LOOP =
            SOUNDS.register("stabilizer_loop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(IcysBetterHorses.MOD_ID, "stabilizer_loop")));

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    private ModSounds() {}
}

