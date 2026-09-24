package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.animal.equine.AbstractHorse;
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
    private CompoundTag saved = new CompoundTag();

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

        initialize(horse, data);
        ArchetypeType archetype = BhBreedData.of(data.bh_getBreedKey()).archetype();

        int rows = data.bh_getChestRows();
        if (lastRows >= 0 && rows < lastRows && horse.level() instanceof ServerLevel level) {
            spillOverflow(horse, data, level, rows);
        }
        lastRows = rows;

        state.tick(horse);
        perks.tick(horse, data, archetype);
        BhSurge.decayAbilities(data);
        for (Slot slot : slots) {
            if (!enabled(slot, data)) {
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

    public void initialize(AbstractHorse horse, IHorseData data) {
        ResourceKey<BreedType> key = data.bh_getBreedKey();
        if (breedInitialized && Objects.equals(key, activeBreedKey)) return;
        if (!horse.level().isClientSide()) {
            snapshot();
            detachAll(horse, data);
            perks.clear(horse);
        }
        breedInitialized = true;
        activeBreedKey = key;
        slots.clear();
        BreedType breed = key == null ? null : BhRegistries.breedTypeRegistry().getValue(key.identifier());
        if (breed != null) {
            CompoundTag tags = saved.getCompoundOrEmpty(key.identifier().toString());
            for (ResourceKey<AbilityType> id : breed.abilities()) {
                AbilityType type = BhRegistries.abilityTypeRegistry().getValue(id.identifier());
                if (type == null) continue;
                BreedAbility ability = type.create();
                if (!horse.level().isClientSide()) ability.load(tags.getCompoundOrEmpty(id.identifier().toString()).copy());
                slots.add(new Slot(id, type, ability));
            }
        }
        if (!horse.level().isClientSide()) perks.onBreedChanged(horse, BhBreedData.of(key).archetype());
    }

    public @Nullable BreedAbility current() {
        return slots.isEmpty() ? null : slots.get(0).ability;
    }

    public List<BreedAbility> all() {
        List<BreedAbility> out = new ArrayList<>(slots.size());
        for (Slot slot : slots) out.add(slot.ability);
        return out;
    }

    public List<ResourceKey<AbilityType>> active(AbstractHorse horse, IHorseData data) {
        initialize(horse, data);
        List<ResourceKey<AbilityType>> out = new ArrayList<>();
        for (Slot slot : slots) {
            if (enabled(slot, data) && slot.ability.hasActiveSkill()) out.add(slot.id);
        }
        return List.copyOf(out);
    }

    public boolean activate(AbstractHorse horse, IHorseData data, ResourceKey<AbilityType> id) {
        if (horse.level().isClientSide()) return false;
        initialize(horse, data);
        for (Slot slot : slots) {
            if (slot.id.equals(id) && enabled(slot, data) && slot.ability.hasActiveSkill()) {
                slot.ability.onActivate(horse, data);
                return true;
            }
        }
        return false;
    }

    private static boolean enabled(Slot slot, IHorseData data) {
        return slot.id.identifier().getNamespace().equals(IcysBetterHorses.MOD_ID)
                && data.bh_getBreed().isRealBreed()
                ? BhConfig.anyAbilityEnabled(data.bh_getBreed()) : slot.type.defaultEnabled();
    }

    public void read(AbstractHorse horse, IHorseData data, CompoundTag tag) {
        if (breedInitialized && !horse.level().isClientSide()) detachAll(horse, data);
        saved = tag.copy();
        breedInitialized = false;
        activeBreedKey = null;
        slots.clear();
    }

    public CompoundTag write() {
        snapshot();
        return saved.copy();
    }

    private void snapshot() {
        if (!breedInitialized || activeBreedKey == null) return;
        String breed = activeBreedKey.identifier().toString();
        CompoundTag tags = saved.getCompoundOrEmpty(breed).copy();
        for (Slot slot : slots) {
            CompoundTag tag = new CompoundTag();
            slot.ability.save(tag);
            tags.put(slot.id.identifier().toString(), tag);
        }
        saved.put(breed, tags);
    }

    @Override
    public void onRemoved(AbstractHorse horse, IHorseData data) {
        if (!horse.level().isClientSide()) detachAll(horse, data);
    }

    private void detachAll(AbstractHorse horse, IHorseData data) {
        for (Slot slot : slots) slot.ability.onDetach(horse, data);
    }

    private static final class Slot {
        private final ResourceKey<AbilityType> id;
        private final AbilityType type;
        private final BreedAbility ability;
        private boolean offered;

        private Slot(ResourceKey<AbilityType> id, AbilityType type, BreedAbility ability) {
            this.id = id;
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
                horse.spawnAtLocation(level, stack);
            }
        }
        chest.setChanged();
    }
}
