package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.BhHorseAttributes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BhBreedDonkey extends BhBreedHorse implements ItemSteerable {

    private final ResourceKey<BreedType> breed;
    private int carrotTicks;

    public BhBreedDonkey(EntityType<? extends Horse> type, Level level, ResourceKey<BreedType> breed) {
        super(type, level);
        this.breed = breed;
    }

    @Override
    public boolean boost() {
        if (level().isClientSide() || carrotTicks > 0
                || !(getControllingPassenger() instanceof Player rider)
                || !rider.isHolding(Items.CARROT_ON_A_STICK)) {
            return false;
        }
        carrotTicks = random.nextInt(841) + 140;
        BhHorseAttributes.apply(this, Attributes.MOVEMENT_SPEED, BhHorseAttributes.Source.ARCHETYPE,
                "carrot_boost", 0.1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && carrotTicks > 0
                && (--carrotTicks == 0 || !(getControllingPassenger() instanceof Player rider)
                || !rider.isHolding(Items.CARROT_ON_A_STICK))) {
            carrotTicks = 0;
            BhHorseAttributes.clear(this, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ARCHETYPE, "carrot_boost");
        }
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return breed;
    }

    @Override
    protected boolean bhIsDonkey() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.DONKEY_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DONKEY_DEATH;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.DONKEY_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DONKEY_HURT;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BhContent.DONKEY.value());
    }

}
