package icy.betterhorses.net;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public final class ModTicketTypes {

    public static final TicketType<ChunkPos> HORSE_TASK =
            TicketType.create("icys-better-horses:horse_task", Comparator.comparingLong(ChunkPos::toLong), 200);

    private ModTicketTypes() {}
}
