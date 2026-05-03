package icy.betterhorses.net.neoforge;

import icy.betterhorses.net.IcysBetterHorses;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(IcysBetterHorses.NEOFORGE_MOD_ID)
public final class IcysBetterHorsesNeoForge {
    public IcysBetterHorsesNeoForge(IEventBus modBus, ModContainer container) {
        IcysBetterHorses.init();
    }
}
