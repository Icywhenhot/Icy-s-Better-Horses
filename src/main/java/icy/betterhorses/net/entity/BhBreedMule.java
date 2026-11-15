package icy.betterhorses.net.entity;

import icy.betterhorses.net.feature.breed.MuleLine;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BhBreedMule extends MediumHorse {

    private static final EntityDataAccessor<String> BH_LINE =
            SynchedEntityData.defineId(BhBreedMule.class, EntityDataSerializers.STRING);
    private static final String LINE_TAG = "BH_MuleLine";

    private final ResourceKey<BreedType> breed;
    private String parsed = "";
    private @Nullable MuleLine line;

    public BhBreedMule(EntityType<? extends Horse> type, Level level, ResourceKey<BreedType> breed) {
        super(type, level);
        this.breed = breed;
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return breed;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BH_LINE, "");
    }

    public @Nullable MuleLine bhLine() {
        String raw = this.entityData.get(BH_LINE);
        if (!raw.equals(parsed)) {
            parsed = raw;
            line = MuleLine.parse(raw);
        }
        return line;
    }

    public void bhSetLine(MuleLine line) {
        this.entityData.set(BH_LINE, line.pack());
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && bhLine() == null) {
            bhSetLine(MuleLine.roll(this.random));
        }
        super.tick();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(LINE_TAG, this.entityData.get(BH_LINE));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BH_LINE, tag.getStringOr(LINE_TAG, ""));
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    protected boolean canParent() {
        return false;
    }

    @Override
    public void setInLove(Player player) {
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.MULE_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.MULE_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MULE_DEATH;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.MULE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.MULE_HURT;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BhContent.MULE.value());
    }
}
