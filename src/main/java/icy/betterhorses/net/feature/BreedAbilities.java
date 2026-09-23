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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BreedAbilities implements HorseFeature {

    private final BhAbilityState state = new BhAbilityState();
    private final ArchetypePerks perks = new ArchetypePerks();
    private @Nullable ResourceKey<BreedType> activeBreedKey;
    private boolean breedInitialized;
    private final List<Slot> slots = new ArrayList<>();
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
            detachAll(horse, data);
            perks.clear(horse);
            activeBreedKey = breedKey;
            slots.clear();
            for (AbilityType type : resolveAbilityTypes(breedKey)) {
                slots.add(new Slot(type, type.create()));
            }
            perks.onBreedChanged(horse, archetype);
        }

        int rows = data.bh_getChestRows();
        if (lastRows >= 0 && rows < lastRows && horse.level() instanceof ServerLevel level) {
            spillOverflow(horse, data, level, rows);
        }
        lastRows = rows;

        state.tick(horse);
        perks.tick(horse, data, archetype);
        BhSurge.decayAbilities(data);
        boolean realBreed = data.bh_getBreed().isRealBreed();
        boolean breedEnabled = realBreed && BhConfig.anyAbilityEnabled(data.bh_getBreed());
        for (Slot slot : slots) {
            boolean enabled = realBreed ? breedEnabled : slot.type.defaultEnabled();
            if (!enabled) {
                if (slot.offered) {
                    slot.ability.onDetach(horse, data);
                    slot.offered = false;
                }
                continue;
            }
            slot.offered = true;
            slot.ability.tick(horse, data, state);
        }
    }

    public @Nullable BreedAbility current() {
        return slots.isEmpty() ? null : slots.get(0).ability;
    }

    public List<BreedAbility> all() {
        List<BreedAbility> out = new ArrayList<>(slots.size());
        for (Slot slot : slots) {
            out.add(slot.ability);
        }
        return out;
    }

    @Override
    public void onRemoved(AbstractHorse horse, IHorseData data) {
        detachAll(horse, data);
    }

    private void detachAll(AbstractHorse horse, IHorseData data) {
        for (Slot slot : slots) {
            slot.ability.onDetach(horse, data);
        }
    }

    private static List<AbilityType> resolveAbilityTypes(@Nullable ResourceKey<BreedType> breedKey) {
        if (breedKey == null) {
            return List.of();
        }
        BreedType type = BhRegistries.breedTypeRegistry().getValue(breedKey.location());
        if (type == null) {
            return List.of();
        }
        List<AbilityType> out = new ArrayList<>();
        for (ResourceKey<AbilityType> abilityKey : type.abilities()) {
            AbilityType abilityType = BhRegistries.abilityTypeRegistry().getValue(abilityKey.location());
            if (abilityType != null) {
                out.add(abilityType);
            }
        }
        return out;
    }

    private static final class Slot {
        private final AbilityType type;
        private final BreedAbility ability;
        private boolean offered;

        private Slot(AbilityType type, BreedAbility ability) {
            this.type = type;
            this.ability = ability;
        }
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
