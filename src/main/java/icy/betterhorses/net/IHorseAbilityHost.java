package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.BreedAbility;
import org.jetbrains.annotations.Nullable;

public interface IHorseAbilityHost {

    @Nullable BreedAbility bh_currentAbility();
}
