package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModTicketTypes {

    public static final TicketType HORSE_TASK = register("horse_task", new TicketType(
            200L,
            TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));

    public static void register(RegisterEvent event) {
        event.register(
                Registries.TICKET_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_task"),
                () -> HORSE_TASK);
    }

    private static TicketType register(String path, TicketType type) {
        return type;
    }

    private ModTicketTypes() {}
}
