package icy.betterhorses.net;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.SOUND_EVENT);
    public static final RegistrySupplier<SoundEvent> CALL_WHISTLE = register("call_whistle");
    public static final RegistrySupplier<SoundEvent> STABILIZER_INTRO = register("stabilizer_intro");
    public static final RegistrySupplier<SoundEvent> STABILIZER_LOOP = register("stabilizer_loop");

    public static void init() {
        SOUNDS.register();
    }

    private static RegistrySupplier<SoundEvent> register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(id));
    }

    private ModSounds() {}
}
