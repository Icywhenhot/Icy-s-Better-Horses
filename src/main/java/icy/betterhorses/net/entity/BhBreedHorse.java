package icy.betterhorses.net.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import icy.betterhorses.net.IHorseData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class BhBreedHorse extends Horse implements BhBreedEntity {

    private static final EntityDataAccessor<Integer> BH_COAT =
            SynchedEntityData.defineId(BhBreedHorse.class, EntityDataSerializers.INT);

    private static final String COAT_TAG = "BH_Coat";

    protected BhBreedHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

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
        int saved = input.getIntOr(COAT_TAG, -1);
        this.entityData.set(BH_COAT, saved < 0 ? bhCoats().roll(this.random) : bhCoats().clamp(saved));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData groupData) {
        SpawnGroupData result =
                super.finalizeSpawn(level, difficulty, reason, groupData);
        bhSetCoat(bhCoats().roll(this.random));
        return result;
    }

    protected void bhInheritCoat(BhBreedHorse parentA, BhBreedHorse parentB) {
        BhBreedHorse source = this.random.nextBoolean() ? parentA : parentB;
        bhSetCoat(source.bhCoat());
    }

    @Override
    public AgeableMob getBreedOffspring(
            ServerLevel level,
            AgeableMob partner) {
        if (partner instanceof BhBreedHorse other) {
            boolean sameBreed = other.getType() == this.getType();
            BhBreedHorse source = sameBreed || this.random.nextBoolean() ? this : other;
            Entity created =
                    source.getType().create(level, EntitySpawnReason.BREEDING);
            if (!(created instanceof BhBreedHorse foal)) {
                return null;
            }
            ((IHorseData) foal).bh_setBreed(source.bhFixedBreed());
            ((IHorseData) foal).bh_setMixedBreed(!sameBreed);
            if (sameBreed) {
                foal.bhInheritCoat(this, other);
            } else {
                foal.bhSetCoat(source.bhCoat());
            }
            return foal;
        }
        return super.getBreedOffspring(level, partner);
    }
}
