package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

public class BhBreedMule extends MediumHorse {

    private final ResourceKey<BreedType> breed;

    public BhBreedMule(EntityType<? extends Horse> type, Level level, ResourceKey<BreedType> breed) {
        super(type, level);
        this.breed = breed;
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return breed;
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
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return EntityType.MULE.getDefaultLootTable();
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
        return bhAttributes(BhContent.MULE.get());
    }
}
