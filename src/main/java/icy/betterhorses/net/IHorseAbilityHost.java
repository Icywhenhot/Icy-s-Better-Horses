package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.registry.AbilityType;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IHorseAbilityHost {

    @Nullable BreedAbility bh_currentAbility();

    List<BreedAbility> bh_allAbilities();

    List<ResourceKey<AbilityType>> bh_activeAbilities();

    boolean bh_activateAbility(ResourceKey<AbilityType> ability);
}
