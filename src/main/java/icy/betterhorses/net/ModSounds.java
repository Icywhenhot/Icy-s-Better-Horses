package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CALL_WHISTLE =
            SOUNDS.register("call_whistle", SoundEvent::createVariableRangeEvent);
    public static final DeferredHolder<SoundEvent, SoundEvent> STABILIZER_INTRO =
            SOUNDS.register("stabilizer_intro", SoundEvent::createVariableRangeEvent);
    public static final DeferredHolder<SoundEvent, SoundEvent> STABILIZER_LOOP =
            SOUNDS.register("stabilizer_loop", SoundEvent::createVariableRangeEvent);

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    private ModSounds() {}
}

