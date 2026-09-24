package icy.betterhorses.net.registry;

import icy.betterhorses.net.feature.breed.BreedAbility;

import java.util.function.Supplier;

public final class AbilityType {

    private final Supplier<BreedAbility> factory;
    private final boolean defaultEnabled;

    public AbilityType(Supplier<BreedAbility> factory, boolean defaultEnabled) {
        this.factory = factory;
        this.defaultEnabled = defaultEnabled;
    }

    public BreedAbility create() {
        return factory.get();
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }
}
