package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

public class BhUndeadHorse extends BhBreedHorse {

    private final ResourceKey<BreedType> breed;
    private final boolean skeleton;
    private final HorseBreed kind;
    private static final String WILD_TAG = "BH_WildUndead";
    private boolean wild;

    public BhUndeadHorse(EntityType<? extends Horse> type, Level level, ResourceKey<BreedType> breed, boolean skeleton) {
        super(type, level);
        this.breed = breed;
        this.skeleton = skeleton;
        this.kind = HorseBreed.byId(breed.location().getPath());
    }

    public HorseBreed bhKind() {
        return kind;
    }

    public void bhMarkWild() {
        wild = true;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return wild && !isTamed() && !isLeashed() && !hasCustomName() && IHorseData.of(this).bh_getOwner() == null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (wild) tag.putBoolean(WILD_TAG, true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        wild = tag.getBoolean(WILD_TAG);
    }

    private boolean swimsDeep() {
        return kind.isDrowned() && BhAbility.DROWNED_SWIM.on();
    }

    @Override
    public void aiStep() {
        if (isAlive() && !level().isClientSide() && !kind.isHusk() && isSunBurnTick()) {
            ItemStack armor = getBodyArmorItem();
            if (armor.isEmpty()) {
                igniteForSeconds(8.0F);
            } else if (armor.isDamageableItem()) {
                Item item = armor.getItem();
                armor.setDamageValue(armor.getDamageValue() + random.nextInt(2));
                if (armor.getDamageValue() >= armor.getMaxDamage()) {
                    onEquippedItemBroken(item, EquipmentSlot.BODY);
                    setBodyArmorItem(ItemStack.EMPTY);
                }
            }
        }
        super.aiStep();
    }

    @Override
    public boolean dampensVibrations() {
        return kind.isSculk() && BhAbility.SCULK_HUSH.on() || super.dampensVibrations();
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
        return bhAttributes(skeleton ? BhContent.SKELETON.get() : BhContent.ZOMBIE.get());
    }

    @Override
    public void bhConvertFrom(AbstractHorse from) {
        if (IHorseData.of(from).bh_getOwner() != null || from.getOwnerUUID() != null) {
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
    public boolean isBaby() {
        return false;
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
        if (skeleton) return 0.96F;
        return swimsDeep() ? 0.95F : super.getWaterSlowDown();
    }

    @Override
    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        return (skeleton || swimsDeep()) && type == NeoForgeMod.WATER_TYPE.value()
                || super.canBeRiddenUnderFluidType(type, rider);
    }

    @Override
    public boolean dismountsUnderwater() {
        return !skeleton && !swimsDeep() && super.dismountsUnderwater();
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return (skeleton ? EntityType.SKELETON_HORSE : EntityType.ZOMBIE_HORSE).getDefaultLootTable();
    }
}
