package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.BhHorseAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

public class BhBreedDonkey extends BhBreedHorse implements ItemSteerable {

    private static final int CARROTS_TO_TAME = 5;
    private static final String CARROTS_FED = "BH_TameCarrots";
    private static final byte TAME_HEARTS = 7;
    private static final byte TAME_SMOKE = 6;

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

    private boolean tamesByHand() {
        return breed.equals(BhContent.MINIATURE_DONKEY.getKey());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (tamesByHand() && !isTamed() && !isVehicle() && !player.isCreative()
                && !isFood(player.getItemInHand(hand))) {
            if (!level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.icys-better-horses.miniature_tame_hint"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack stack) {
        boolean ate = super.handleEating(player, stack);
        if (!tamesByHand() || isTamed() || !stack.is(Items.GOLDEN_CARROT) || level().isClientSide()) {
            return ate;
        }
        CompoundTag data = getPersistentData();
        int fed = Math.min(data.getInt(CARROTS_FED), CARROTS_TO_TAME - 1);
        if (random.nextInt(CARROTS_TO_TAME - fed) == 0 && tameWithName(player)) {
            data.remove(CARROTS_FED);
            level().broadcastEntityEvent(this, TAME_HEARTS);
        } else {
            data.putInt(CARROTS_FED, fed + 1);
            level().broadcastEntityEvent(this, TAME_SMOKE);
        }
        return true;
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
        return bhAttributes(BhContent.DONKEY.get());
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return EntityType.DONKEY.getDefaultLootTable();
    }
}
