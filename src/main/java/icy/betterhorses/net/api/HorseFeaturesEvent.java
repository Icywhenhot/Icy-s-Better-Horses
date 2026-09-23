package icy.betterhorses.net.api;

import icy.betterhorses.net.feature.HorseFeature;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HorseFeaturesEvent extends Event {
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
}
