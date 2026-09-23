package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.BreedAbility;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IHorseAbilityHost {

    @Nullable BreedAbility bh_currentAbility();

    List<BreedAbility> bh_allAbilities();
}
