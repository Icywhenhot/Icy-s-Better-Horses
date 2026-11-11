package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BhUndeadHorse extends BhBreedHorse {

    private final ResourceKey<BreedType> breed;
    private final boolean skeleton;

    public BhUndeadHorse(EntityType<? extends Horse> type, Level level, ResourceKey<BreedType> breed, boolean skeleton) {
        super(type, level);
        this.breed = breed;
        this.skeleton = skeleton;
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return breed;
    }

    public boolean bhIsSkeleton() {
        return skeleton;
    }

    public boolean bhIsZombie() {
        return !skeleton;
    }

    public static AttributeSupplier.Builder createAttributes(boolean skeleton) {
        return bhAttributes(skeleton ? BhContent.SKELETON.value() : BhContent.ZOMBIE.value());
    }

    @Override
    public void bhConvertFrom(AbstractHorse from) {
        if (IHorseData.of(from).bh_getOwner() != null || from.getOwnerReference() != null) {
            setHealth(Math.min(from.getHealth(), getMaxHealth()));
            return;
        }
        double fraction = from.getHealth() / from.getMaxHealth();
        ArchetypeType arch = BhBreedData.of(breed).archetype();
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(arch.rollSpeed(random));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(arch.rollHealth(random));
        getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(arch.rollJump(random));
        setHealth((float) (getMaxHealth() * fraction));
    }

    @Override
    protected void addBehaviourGoals() {
    }

    @Override
    public void setAge(int age) {
        super.setAge(0);
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
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public void setInLove(Player player) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return skeleton ? isEyeInFluid(FluidTags.WATER) ? SoundEvents.SKELETON_HORSE_AMBIENT_WATER
                : SoundEvents.SKELETON_HORSE_AMBIENT : SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return skeleton ? SoundEvents.SKELETON_HORSE_HURT : SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return skeleton ? SoundEvents.SKELETON_HORSE_DEATH : SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        if (!skeleton) return super.getSwimSound();
        if (onGround()) {
            if (!isVehicle()) return SoundEvents.SKELETON_HORSE_STEP_WATER;
            gallopSoundCounter++;
            if (gallopSoundCounter > 5 && gallopSoundCounter % 3 == 0) return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
            if (gallopSoundCounter <= 5) return SoundEvents.SKELETON_HORSE_STEP_WATER;
        }
        return SoundEvents.SKELETON_HORSE_SWIM;
    }

    @Override
    protected void playSwimSound(float volume) {
        super.playSwimSound(skeleton ? onGround() ? 0.3F : Math.min(0.1F, volume * 25.0F) : volume);
    }

    @Override
    protected void playJumpSound() {
        if (skeleton && isInWater()) {
            playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4F, 1.0F);
        } else {
            super.playJumpSound();
        }
    }

    @Override
    protected float getWaterSlowDown() {
        return skeleton ? 0.96F : super.getWaterSlowDown();
    }

    @Override
    public boolean dismountsUnderwater() {
        return !skeleton && super.dismountsUnderwater();
    }
}
