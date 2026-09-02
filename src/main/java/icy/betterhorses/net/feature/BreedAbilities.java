package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.feature.breed.BhAbilityState;
import icy.betterhorses.net.feature.breed.BreedAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BreedAbilities implements HorseFeature {

    private final BhAbilityState state = new BhAbilityState();
    private final ArchetypePerks perks = new ArchetypePerks();
    private @Nullable HorseBreed active;
    private @Nullable BreedAbility ability;
    private int lastRows = -1;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide()) {
            return;
        }
        BhSurge.decay(data);
        int stomping = data.bh_getStompTicks();
        if (stomping > 0) {
            data.bh_setStompTicks(stomping - 1);
        }

        HorseBreed breed = data.bh_getBreed();
        if (breed != active) {
            if (ability != null) {
                ability.onDetach(horse, data);
            }
            perks.clear(horse);
            active = breed;
            ability = breed.newAbility();
            perks.onBreedChanged(horse, breed.archetype());
        }

        int rows = data.bh_getChestRows();
        if (lastRows >= 0 && rows < lastRows && horse.level() instanceof ServerLevel level) {
            spillOverflow(horse, data, level, rows);
        }
        lastRows = rows;

        state.tick(horse);
        perks.tick(horse, data, breed.archetype());
        if (ability != null) {
            ability.tick(horse, data, state);
        }
    }

    public @Nullable BreedAbility current() {
        return ability;
    }

    private static void spillOverflow(AbstractHorse horse, IHorseData data,
                                      ServerLevel level, int rows) {
        SimpleContainer chest = data.bh_getChestContainer();
        for (int i = rows * 9; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty()) {
                chest.setItem(i, ItemStack.EMPTY);
                horse.spawnAtLocation(level, stack);
            }
        }
        chest.setChanged();
    }
}
