package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, IcysBetterHorses.MOD_ID);

    public static final SoundEvent CALL_WHISTLE = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "call_whistle"));
    public static final SoundEvent STABILIZER_INTRO = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stabilizer_intro"));
    public static final SoundEvent STABILIZER_LOOP = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stabilizer_loop"));

    @SuppressWarnings("unused") private static final DeferredHolder<SoundEvent, SoundEvent> CALL_WHISTLE_ENTRY = SOUNDS.register("call_whistle", () -> CALL_WHISTLE);
    @SuppressWarnings("unused") private static final DeferredHolder<SoundEvent, SoundEvent> STABILIZER_INTRO_ENTRY = SOUNDS.register("stabilizer_intro", () -> STABILIZER_INTRO);
    @SuppressWarnings("unused") private static final DeferredHolder<SoundEvent, SoundEvent> STABILIZER_LOOP_ENTRY = SOUNDS.register("stabilizer_loop", () -> STABILIZER_LOOP);

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    private ModSounds() {}
}

