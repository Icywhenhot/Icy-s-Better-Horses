package icy.betterhorses.net.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Shared base for every dedicated breed mob.
 *
 * <p>Extends vanilla {@link Horse} so all the mod's existing mixins - taming, bonding, gear,
 * cart, whistle, roster - keep working untouched. What this adds is the one thing every breed
 * needs and vanilla has no concept of: its own coat, independent of {@code Variant} and
 * {@code Markings}.
 *
 * <p>A new breed is therefore about twenty lines: extend this, return a breed and a coat list.
 */
public abstract class BhBreedHorse extends Horse implements BhBreedEntity {

    private static final EntityDataAccessor<Integer> BH_COAT =
            SynchedEntityData.defineId(BhBreedHorse.class, EntityDataSerializers.INT);

    private static final String COAT_TAG = "BH_Coat";

    protected BhBreedHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    /** The coats this breed can wear. Never {@code null}. */
    public abstract BhBreedCoats bhCoats();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BH_COAT, 0);
    }

    public int bhCoat() {
        return bhCoats().clamp(this.entityData.get(BH_COAT));
    }

    public void bhSetCoat(int index) {
        this.entityData.set(BH_COAT, bhCoats().clamp(index));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(COAT_TAG, this.entityData.get(BH_COAT));
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // -1 marks "never rolled", so a horse from an older save still gets a coat
        int saved = input.getIntOr(COAT_TAG, -1);
        this.entityData.set(BH_COAT, saved < 0 ? bhCoats().roll(this.random) : bhCoats().clamp(saved));
    }

    /**
     * Rolls a coat on first spawn. Deliberately not gated on the current value: the synched
     * default of 0 cannot be told apart from a legitimately rolled 0.
     */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.EntitySpawnReason reason,
            net.minecraft.world.entity.SpawnGroupData groupData) {
        net.minecraft.world.entity.SpawnGroupData result =
                super.finalizeSpawn(level, difficulty, reason, groupData);
        bhSetCoat(bhCoats().roll(this.random));
        return result;
    }

    /** Foals inherit one parent's coat at random rather than re-rolling from scratch. */
    protected void bhInheritCoat(BhBreedHorse parentA, BhBreedHorse parentB) {
        BhBreedHorse source = this.random.nextBoolean() ? parentA : parentB;
        bhSetCoat(source.bhCoat());
    }

    /**
     * A breed bred to its own kind breeds true; anything else falls back to vanilla's
     * mixing rules.
     *
     * <p>Written once here rather than per breed. Every breed's version of this was the
     * same eighteen lines with the type name swapped, which is a copy-paste bug waiting to
     * happen - miss one substitution and a Thoroughbred pair produces an Icelandic foal.
     * Matching on {@code getType()} makes that impossible to get wrong, and a new breed
     * inherits correct breeding without writing anything.
     */
    @Override
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.AgeableMob partner) {
        if (partner instanceof BhBreedHorse other && other.getType() == this.getType()) {
            // spawn reason must be explicit: a bare null is ambiguous between the
            // EntitySpawnReason and EntitySpawnRequest overloads
            net.minecraft.world.entity.Entity created =
                    this.getType().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
            if (!(created instanceof BhBreedHorse foal)) {
                return null;
            }
            ((icy.betterhorses.net.IHorseData) foal).bh_setBreed(bhFixedBreed());
            ((icy.betterhorses.net.IHorseData) foal).bh_setMixedBreed(false);
            foal.bhInheritCoat(this, other);
            return foal;
        }
        return super.getBreedOffspring(level, partner);
    }
}
