package icy.betterhorses.net.api;

import icy.betterhorses.net.feature.HorseFeature;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HorseFeaturesEvent {

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, listeners -> event -> {
        for (Listener listener : listeners) {
            listener.collect(event);
        }
    });

    private final AbstractHorse horse;
    private final List<HorseFeature> features = new ArrayList<>();

    public HorseFeaturesEvent(AbstractHorse horse) {
        this.horse = horse;
    }

    public AbstractHorse horse() {
        return horse;
    }

    public void add(HorseFeature feature) {
        features.add(Objects.requireNonNull(feature));
    }

    public List<HorseFeature> features() {
        return List.copyOf(features);
    }

    @FunctionalInterface
    public interface Listener {
        void collect(HorseFeaturesEvent event);
    }
}
