package icy.betterhorses.net.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    protected void bhInheritCoat(BhBreedHorse parentA, BhBreedHorse parentB) {
        BhBreedHorse source = this.random.nextBoolean() ? parentA : parentB;
        bhSetCoat(source.bhCoat());
    }

    @Override
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.AgeableMob partner) {
        if (partner instanceof BhBreedHorse other && other.getType() == this.getType()) {
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
