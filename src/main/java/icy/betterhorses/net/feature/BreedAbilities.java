package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.feature.breed.BhAbilityState;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.registry.AbilityType;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class BreedAbilities implements HorseFeature {

    private final BhAbilityState state = new BhAbilityState();
    private final ArchetypePerks perks = new ArchetypePerks();
    private @Nullable ResourceKey<BreedType> activeBreedKey;
    private boolean breedInitialized;
    private @Nullable AbilityType activeAbilityType;
    private @Nullable BreedAbility ability;
    private boolean offered;
    private int lastRows = -1;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide()) {
            return;
        }
        BhSurge.decay(data);
        BhSurge.decayPerk(data);
        int stomping = data.bh_getStompTicks();
        if (stomping > 0) {
            data.bh_setStompTicks(stomping - 1);
        }

        ResourceKey<BreedType> breedKey = data.bh_getBreedKey();
        ArchetypeType archetype = BhBreedData.of(breedKey).archetype();
        if (!breedInitialized || !Objects.equals(breedKey, activeBreedKey)) {
            breedInitialized = true;
            if (ability != null) {
                ability.onDetach(horse, data);
            }
            perks.clear(horse);
            activeBreedKey = breedKey;
            activeAbilityType = resolveAbilityType(breedKey);
            ability = activeAbilityType == null ? null : activeAbilityType.create();
            perks.onBreedChanged(horse, archetype);
        }

        int rows = data.bh_getChestRows();
        if (lastRows >= 0 && rows < lastRows && horse.level() instanceof ServerLevel level) {
            spillOverflow(horse, data, level, rows);
        }
        lastRows = rows;

        state.tick(horse);
        perks.tick(horse, data, archetype);
        if (ability == null) {
            return;
        }
        boolean enabled = data.bh_getBreed().isRealBreed()
                ? BhConfig.anyAbilityEnabled(data.bh_getBreed())
                : activeAbilityType.defaultEnabled();
        if (!enabled) {
            if (offered) {
                ability.onDetach(horse, data);
                offered = false;
            }
            return;
        }
        offered = true;
        ability.tick(horse, data, state);
    }

    public @Nullable BreedAbility current() {
        return ability;
    }

    @Override
    public void onRemoved(AbstractHorse horse, IHorseData data) {
        if (ability != null) ability.onDetach(horse, data);
    }

    private static @Nullable AbilityType resolveAbilityType(@Nullable ResourceKey<BreedType> breedKey) {
        if (breedKey == null) {
            return null;
        }
        BreedType type = BhRegistries.breedTypeRegistry().getValue(breedKey.location());
        if (type == null || type.abilities().isEmpty()) {
            return null;
        }
        ResourceKey<AbilityType> abilityKey = type.abilities().get(0);
        return BhRegistries.abilityTypeRegistry().getValue(abilityKey.location());
    }

    private static void spillOverflow(AbstractHorse horse, IHorseData data,
                                      ServerLevel level, int rows) {
        SimpleContainer chest = data.bh_getChestContainer();
        for (int i = rows * 9; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty()) {
                chest.setItem(i, ItemStack.EMPTY);
                horse.spawnAtLocation(stack);
            }
        }
        chest.setChanged();
    }
}
