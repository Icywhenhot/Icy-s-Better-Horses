package icy.betterhorses.net.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class HorseRecallEvent extends Event {
    private final ServerPlayer player;
    private final AbstractHorse horse;

    protected HorseRecallEvent(ServerPlayer player, AbstractHorse horse) {
        this.player = player;
        this.horse = horse;
    }

    public ServerPlayer player() {
        return player;
    }

    public AbstractHorse horse() {
        return horse;
    }

    //I make honse teleport now
    @Cancelable
    public static final class Summon extends HorseRecallEvent {
        public Summon(ServerPlayer player, AbstractHorse horse) {
            super(player, horse);
        }
    }

//stuff for unloaded honses, to bring them from the snapshot version
    public static final class Respawned extends HorseRecallEvent {
        public Respawned(ServerPlayer player, AbstractHorse horse) {
            super(player, horse);
        }
    }
}
