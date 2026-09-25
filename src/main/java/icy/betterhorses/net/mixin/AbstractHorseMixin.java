package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.ModSounds;
import icy.betterhorses.net.BhCriteria;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.BhHorseInteraction;
import icy.betterhorses.net.BhHorseStorage;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhVanillaHorseSwap;
import icy.betterhorses.net.BhHorseSteering;
import icy.betterhorses.net.BhHorseAttachments;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.CommandType;
import icy.betterhorses.net.registry.GenderType;
import icy.betterhorses.net.registry.SpeciesType;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.feature.BreedAbilities;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.feature.CartRig;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.feature.FrostHooves;
import icy.betterhorses.net.feature.HorseFeature;
import icy.betterhorses.net.api.HorseFeaturesEvent;
import icy.betterhorses.net.registry.AbilityType;
import icy.betterhorses.net.feature.RiderGate;
import icy.betterhorses.net.feature.Stabilizer;
import icy.betterhorses.net.feature.SpeedRecord;
import icy.betterhorses.net.feature.SaddleWatch;
import icy.betterhorses.net.feature.SwimBoost;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.inventory.CartChestMenu;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.DefendOwnerGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.goal.SpookGoal;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import icy.betterhorses.net.network.HorseJumpPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements IHorseData, IHorseAbilityHost {

    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    private int eatingCounter;

    @Shadow
    private int standCounter;

    @Shadow
    protected abstract void doPlayerRide(Player player);


    @Unique private volatile @Nullable UUID bh_owner = null;
    @Unique private ResourceKey<CommandType> bh_command = BhContent.COMMAND_FOLLOW.getKey();
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable ResourceKey<Level> bh_homeDim = null;
    @Unique private @Nullable BlockPos bh_wanderCenter = null;
    @Unique private int bh_bond = 0;
    @Unique private boolean bh_nameTagBondReceived = false;
    @Unique private int bh_generation = 0;
    @Unique
    private final SimpleContainer bh_gearContainer = new SimpleContainer(GearSlot.COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            AbstractHorseMixin.this.bh_syncGearFlags();
        }
    };
    @Unique private static final int BH_CHEST_MAX_SLOTS = 54;
    @Unique private final SimpleContainer bh_chestContainer = new SimpleContainer(BH_CHEST_MAX_SLOTS);
    @Unique private static final int BH_CART_CHEST_SIZE = CartChestMenu.SLOTS;
    @Unique private @Nullable SimpleContainer bh_cartChestContainer;
    @Unique private ItemStack bh_cartPlow = ItemStack.EMPTY;
    @Unique private ItemStack bh_cartChestItem = ItemStack.EMPTY;
    @Unique private boolean bh_fedGoldenAppleThisTick = false;
    @Unique private static final float BH_HURT_NEIGH_CHANCE = 0.3F;
    @Unique private static final int BH_GRAZE_ROLL_INTERVAL = 1200;
    @Unique private static final int BH_GRAZE_HURT_COOLDOWN_TICKS = 200;
    @Unique private int bh_grazeBlockedUntilTick = 0;
    @Unique private int bh_gear = 0;
    @Unique private int bh_jumpCue;
    @Unique private @Nullable UUID bh_combatTarget = null;
    @Unique private boolean bh_abilityPaused = false;
    @Unique private int bh_spookTicks = 0;

    @Unique private final SaddleWatch bh_saddle = new SaddleWatch();
    @Unique private final CartRig bh_cartRig = new CartRig();
    @Unique private final HorseCombat bh_combat = new HorseCombat();
    @Unique private final BreedAbilities bh_abilities = new BreedAbilities();

    @Unique
    private boolean bh_ours() {
        return BhHorseKind.managed((AbstractHorse) (Object) this);
    }

    @Unique private HorseFeature[] bh_features;

    @Unique
    private HorseFeature[] bh_features() {
        if (bh_features == null) {
            bh_features = new HorseFeature[]{
            bh_saddle,
            (horse, data) -> bh_clearGearWhenUnridden(horse),
            new SpeedRecord(),
            new RiderGate(),
            new Stabilizer(),
            bh_cartRig,
            new SwimBoost(),
            new FrostHooves(),
            bh_combat,
            bh_abilities,
            };
            if (bh_ours()) {
                HorseFeaturesEvent event = new HorseFeaturesEvent((AbstractHorse) (Object) this);
                NeoForge.EVENT_BUS.post(event);
                java.util.List<HorseFeature> features = new java.util.ArrayList<>(java.util.Arrays.asList(bh_features));
                features.addAll(event.features());
                bh_features = features.toArray(HorseFeature[]::new);
            }
        }
        return bh_features;
    }

    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable UUID bh_getOwner() {
        if (level().isClientSide()) {
            return bh_parseOwner(((AbstractHorse) (Object) this).getData(BhHorseAttachments.OWNER));
        }
        return bh_owner;
    }

    @Override
    public void bh_setOwner(@Nullable UUID owner) {
        this.bh_owner = owner;
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.OWNER, owner == null ? "" : owner.toString());
        if (!level().isClientSide()) {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (owner != null) {
                HorseTracker.register(self);
            } else {
                HorseTracker.disown(self);
            }
        }
    }

    @Unique
    private static @Nullable UUID bh_parseOwner(String synced) {
        if (synced == null || synced.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(synced);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public ResourceKey<CommandType> bh_getCommand() {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            ResourceLocation loc = ResourceLocation.tryParse(((AbstractHorse) (Object) this).getData(BhHorseAttachments.COMMAND));
            return loc != null ? ResourceKey.create(BhRegistries.COMMAND_TYPES, loc) : BhContent.COMMAND_FOLLOW.getKey();
        }
        return bh_command;
    }

    @Override
    public void bh_setCommand(ResourceKey<CommandType> command) {
        this.bh_command = command;
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.COMMAND, command.location().toString());
    }

    @Override
    public @Nullable BlockPos bh_getHome() {
        return bh_home;
    }

    @Override
    public void bh_setHome(@Nullable BlockPos pos) {
        this.bh_home = pos;
        this.bh_homeDim = pos == null ? null : ((AbstractHorse) (Object) this).level().dimension();
    }

    @Override
    public @Nullable ResourceKey<Level> bh_getHomeDimension() {
        return this.bh_homeDim;
    }

    @Override
    public @Nullable BlockPos bh_getWanderCenter() {
        return bh_wanderCenter;
    }

    @Override
    public void bh_setWanderCenter(@Nullable BlockPos pos) {
        this.bh_wanderCenter = pos == null ? null : pos.immutable();
    }

    @Override
    public int bh_getBond() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.BOND);
    }

    @Override
    public void bh_setBond(int level) {
        int previous = this.bh_bond;
        this.bh_bond = Math.max(0, Math.min(100, level));
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BOND, this.bh_bond);
        bh_applyBondAttributes();
        if (previous < 100 && this.bh_bond >= 100) {
            bh_awardOwner(BhCriteria.BOND_MAX);
        }
    }

    @Unique
    private void bh_awardOwner(String key) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;
        UUID owner = this.bh_getOwner();
        MinecraftServer server = self.level().getServer();
        if (owner == null || server == null) return;
        BhCriteria.fire(server.getPlayerList().getPlayer(owner), key);
    }

    @Override
    public int bh_getGeneration() {
        return this.bh_generation;
    }

    @Override
    public void bh_setGeneration(int generation) {
        this.bh_generation = generation;
    }

    @Override
    public boolean bh_hasReceivedNameTagBond() {
        return this.bh_nameTagBondReceived;
    }

    @Override
    public void bh_setReceivedNameTagBond(boolean received) {
        this.bh_nameTagBondReceived = received;
    }

    @Override
    public ResourceKey<GenderType> bh_getGender() {
        ResourceLocation loc = ResourceLocation.tryParse(((AbstractHorse) (Object) this).getData(BhHorseAttachments.GENDER));
        return loc != null ? ResourceKey.create(BhRegistries.GENDER_TYPES, loc) : BhContent.MALE.getKey();
    }

    @Override
    public void bh_setGender(ResourceKey<GenderType> gender) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.GENDER, gender.location().toString());
    }

    @Override
    public HorseBreed bh_getBreed() {
        ResourceKey<BreedType> key = bh_getBreedKey();
        if (key != null) {
            HorseBreed mapped = HorseBreed.byId(key.location().getPath());
            return mapped.isRealBreed() ? mapped : HorseBreed.UNKNOWN_SPECIES;
        }
        ResourceKey<SpeciesType> species = bh_getSpecies();
        if (species.equals(BhContent.SPECIES_DONKEY.getKey())) return HorseBreed.DONKEY_SPECIES;
        if (species.equals(BhContent.SPECIES_MULE.getKey())) return HorseBreed.MULE_SPECIES;
        if (species.equals(BhContent.SPECIES_SKELETON.getKey())) return HorseBreed.SKELETON_SPECIES;
        if (species.equals(BhContent.SPECIES_ZOMBIE.getKey())) return HorseBreed.ZOMBIE_SPECIES;
        return HorseBreed.UNKNOWN_SPECIES;
    }

    @Override
    public void bh_setBreed(HorseBreed breed) {
        ResourceKey<BreedType> key = BhBreeds.keyOf(breed);
        if (key != null) {
            bh_setBreedKey(key);
        } else {
            bh_setSpecies(BhBreeds.speciesOf(breed));
        }
    }

    @Override
    public @Nullable ResourceKey<BreedType> bh_getBreedKey() {
        if ((Object) this instanceof BhBreedEntity breedEntity) {
            return breedEntity.bhFixedBreed();
        }
        String raw = ((AbstractHorse) (Object) this).getData(BhHorseAttachments.BREED);
        if (raw.isEmpty()) {
            return null;
        }
        ResourceLocation loc = ResourceLocation.tryParse(raw);
        return loc == null ? null : ResourceKey.create(BhRegistries.BREED_TYPES, loc);
    }

    @Override
    public void bh_setBreedKey(@Nullable ResourceKey<BreedType> breed) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, breed == null ? "" : breed.location().toString());
        if (breed != null) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhContent.SPECIES_NONE.getKey().location().toString());
        }
    }

    @Override
    public ResourceKey<SpeciesType> bh_getSpecies() {
        if ((Object) this instanceof BhBreedEntity) {
            return BhContent.SPECIES_NONE.getKey();
        }
        ResourceLocation loc = ResourceLocation.tryParse(((AbstractHorse) (Object) this).getData(BhHorseAttachments.SPECIES));
        return loc != null ? ResourceKey.create(BhRegistries.SPECIES_TYPES, loc) : BhContent.SPECIES_NONE.getKey();
    }

    @Override
    public void bh_setSpecies(ResourceKey<SpeciesType> species) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, species.location().toString());
        if (!species.equals(BhContent.SPECIES_NONE.getKey())) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, "");
        }
    }

    @Override
    public boolean bh_isMixedBreed() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.BREED_MIXED);
    }

    @Override
    public void bh_setMixedBreed(boolean mixed) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED_MIXED, mixed);
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return HorseStabilizerState.fromId(((AbstractHorse) (Object) this).getData(BhHorseAttachments.STABILIZER_STATE));
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.STABILIZER_STATE, state.ordinal());
    }

    @Override
    public int bh_getGearFlags() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.GEAR_FLAGS);
    }

    @Override
    public boolean bh_hasUpgradedSaddle() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        return self.level().isClientSide()
                ? self.getData(BhHorseAttachments.UPGRADED_SADDLE)
                : inventory != null && inventory.getItem(0).is(ModItems.UPGRADED_SADDLE);
    }

    @Override
    public SimpleContainer bh_getGearContainer() {
        return bh_gearContainer;
    }

    @Override
    public SimpleContainer bh_getChestContainer() {
        return bh_chestContainer;
    }

    @Override
    public boolean bh_hasChestGear() {
        ItemStack chestGear = bh_gearContainer.getItem(GearSlot.CHEST.ordinal());
        return GearSlot.isChest(chestGear);
    }

    @Override
    public void bh_onChestGearRemoved(ItemStack previousChestGear) {
        if (GearSlot.isStorageChest(previousChestGear)) {
            bh_dropChestContents();
        }
    }

    @Override
    public void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        bh_dropCartChest();
        bh_dropCartPlough();
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_gearContainer);
        bh_dropChestContents();
        bh_syncGearFlags();
    }

    @Override
    public @Nullable HorseCartEntity bh_getCartEntity() {
        return this.bh_cartRig.cart();
    }

    @Unique private @Nullable UUID bh_cartId;
    @Unique private int bh_bondRemainder;
    @Unique private long bh_rescueReadyAt;

    @Override
    public int bh_getBondRemainder() { return bh_bondRemainder; }

    @Override
    public void bh_setBondRemainder(int value) { bh_bondRemainder = value; }

    @Override
    public long bh_getRescueReadyAt() { return bh_rescueReadyAt; }

    @Override
    public void bh_setRescueReadyAt(long value) { bh_rescueReadyAt = value; }

    @Override
    public void bh_onRemoved() {
        for (HorseFeature feature : bh_features()) feature.onRemoved((AbstractHorse) (Object) this, this);
    }

    @Override
    public @Nullable UUID bh_getCartId() {
        return bh_cartId;
    }

    @Override
    public void bh_setCartId(@Nullable UUID id) {
        bh_cartId = id;
    }

    @Override
    public boolean bh_hasLargeCart() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART_LARGE);
    }

    @Override
    public void bh_setLargeCart(boolean large) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.CART_LARGE, large && this.bh_mayUseLargeCart());
    }

    @Override
    public boolean bh_hasCartChest() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART_CHEST);
    }

    @Override
    public void bh_setCartChest(ItemStack chest) {
        bh_cartChestItem = chest;
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.CART_CHEST, !chest.isEmpty());
    }

    @Override
    public SimpleContainer bh_getCartChestContainer() {
        if (bh_cartChestContainer == null) {
            bh_cartChestContainer = new SimpleContainer(BH_CART_CHEST_SIZE);
        }
        return bh_cartChestContainer;
    }

    @Override
    public void bh_dropCartChest() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || !bh_hasCartChest()) {
            return;
        }
        ItemStack chest = bh_cartChestItem.isEmpty() ? new ItemStack(Items.CHEST) : bh_cartChestItem;
        bh_setCartChest(ItemStack.EMPTY);
        if (bh_cartChestContainer != null) {
            BhHorseStorage.dropContainerContents(self, serverLevel, bh_cartChestContainer);
        }
        self.spawnAtLocation(chest);
    }

    @Override
    public boolean bh_hasCartPlough() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART_PLOW);
    }

    @Override
    public ItemStack bh_getCartPlough() {
        return bh_cartPlow;
    }

    @Override
    public void bh_setCartPlough(ItemStack hoe) {
        bh_cartPlow = hoe;
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.CART_PLOW, !hoe.isEmpty());
    }

    @Override
    public void bh_dropCartPlough() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || bh_cartPlow.isEmpty()) {
            return;
        }
        ItemStack hoe = bh_cartPlow;
        bh_setCartPlough(ItemStack.EMPTY);
        self.spawnAtLocation(hoe);
    }

    @Override
    public boolean bh_hasAnyEquipment() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.isSaddled()
                || !self.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return true;
        }
        return !this.inventory.isEmpty() || !bh_gearContainer.isEmpty() || !bh_chestContainer.isEmpty()
                || bh_hasCartChest() || bh_hasCartPlough();
    }

    @Override
    public void bh_disown() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        self.ejectPassengers();
        self.setOwnerUUID(null);
        self.setTamed(false);
        bh_setBond(0);
        bh_setHome(null);
        bh_setWanderCenter(self.blockPosition());
        bh_setCommand(BhContent.COMMAND_WANDER.getKey());
        bh_setOwner(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(CompoundTag output, CallbackInfo ci) {
        if (bh_ours()) output.put("BH_Abilities", bh_abilities.write());
        if (bh_owner != null) {
            output.putUUID("BH_Owner", bh_owner);
        }
        output.putInt("BH_AbilityPaused", bh_abilityPaused ? 1 : 0);
        output.putString("BH_CommandId", bh_command.location().toString());
        output.putInt("BH_Bond", bh_bond);
        output.putInt("BH_BondRemainder", bh_bondRemainder);
        output.putLong("BH_RescueReadyAt", bh_rescueReadyAt);
        output.putInt("BH_Generation", bh_generation);
        output.putInt("BH_NameTagBondGiven", bh_nameTagBondReceived ? 1 : 0);
        if (bh_home != null) {
            bh_writeBlockPos(output, "BH_Home", bh_home);
        }
        if (bh_homeDim != null) {
            output.putString("BH_HomeDim", bh_homeDim.location().toString());
        }
        if (bh_wanderCenter != null) {
            bh_writeBlockPos(output, "BH_WanderCenter", bh_wanderCenter);
        }
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseStorage.writeContainer(output, "BH_Gear", bh_gearContainer, self.registryAccess());
        BhHorseStorage.writeContainer(output, "BH_Chest", bh_chestContainer, self.registryAccess());
        output.putBoolean("BH_CartChestOn", ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART_CHEST));
        if (!bh_cartChestItem.isEmpty()) {
            output.put("BH_CartChestItem", bh_cartChestItem.save(self.registryAccess()));
        }
        output.putBoolean("BH_CartLarge", ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART_LARGE));
        if (bh_cartId != null) output.putUUID("BH_CartId", bh_cartId);
        if (bh_cartChestContainer != null) {
            BhHorseStorage.writeContainer(output, "BH_CartChest", bh_cartChestContainer, self.registryAccess());
        }
        if (!bh_cartPlow.isEmpty()) {
            output.put("BH_CartPlow", bh_cartPlow.save(self.registryAccess()));
        }
        output.putString("BH_GenderId", ((AbstractHorse) (Object) this).getData(BhHorseAttachments.GENDER));
        String breedRaw = ((AbstractHorse) (Object) this).getData(BhHorseAttachments.BREED);
        if (!breedRaw.isEmpty()) {
            output.putString("BH_BreedId", breedRaw);
        }
        output.putString("BH_SpeciesId", ((AbstractHorse) (Object) this).getData(BhHorseAttachments.SPECIES));
        output.putBoolean("BH_BreedMixed", ((AbstractHorse) (Object) this).getData(BhHorseAttachments.BREED_MIXED));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(CompoundTag input, CallbackInfo ci) {
        bh_owner = input.hasUUID("BH_Owner") ? input.getUUID("BH_Owner") : null;
        bh_cartId = input.hasUUID("BH_CartId") ? input.getUUID("BH_CartId") : null;
        bh_abilityPaused = input.getInt("BH_AbilityPaused") != 0;
        if (bh_owner == null) {
            bh_owner = ((AbstractHorse) (Object) this).getOwnerUUID();
        }
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.OWNER, bh_owner == null ? "" : bh_owner.toString());
        if (input.contains("BH_CommandId", Tag.TAG_STRING)) {
            ResourceLocation loc = ResourceLocation.tryParse(input.getString("BH_CommandId"));
            bh_command = loc != null ? ResourceKey.create(BhRegistries.COMMAND_TYPES, loc) : BhContent.COMMAND_FOLLOW.getKey();
        } else if (input.contains("BH_Command", Tag.TAG_INT)) {
            bh_command = bh_legacyCommandKey(input.getInt("BH_Command"));
        } else {
            bh_command = BhContent.COMMAND_FOLLOW.getKey();
        }
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.COMMAND, bh_command.location().toString());
        bh_bond = input.getInt("BH_Bond");
        bh_bondRemainder = Math.floorMod(input.getInt("BH_BondRemainder"), 2);
        bh_rescueReadyAt = input.getLong("BH_RescueReadyAt");
        bh_generation = input.getInt("BH_Generation");
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BOND, bh_bond);
        bh_nameTagBondReceived = input.contains("BH_NameTagBondGiven")
                ? input.getInt("BH_NameTagBondGiven") != 0
                : bh_bond > 0;
        bh_home = BhHorseStorage.readLegacyBlockPos(input, "BH_Home");
        bh_homeDim = input.contains("BH_HomeDim", Tag.TAG_STRING)
                ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(input.getString("BH_HomeDim")))
                : null;
        bh_wanderCenter = BhHorseStorage.readLegacyBlockPos(input, "BH_WanderCenter");
        if (bh_home != null && bh_homeDim == null) {
            bh_homeDim = ((AbstractHorse) (Object) this).level().dimension();
        }
        bh_applyBondAttributes();
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseStorage.readContainer(input, "BH_Gear", bh_gearContainer, self.registryAccess());
        BhHorseStorage.readContainer(input, "BH_Chest", bh_chestContainer, self.registryAccess());
        bh_setCartChest(!input.getBoolean("BH_CartChestOn") ? ItemStack.EMPTY
                : input.contains("BH_CartChestItem", Tag.TAG_COMPOUND)
                ? ItemStack.parse(self.registryAccess(), input.getCompound("BH_CartChestItem")).orElseGet(() -> new ItemStack(Items.CHEST))
                : new ItemStack(Items.CHEST));
        if (bh_hasCartChest()) {
            BhHorseStorage.readContainer(input, "BH_CartChest", bh_getCartChestContainer(), self.registryAccess());
        }
        bh_setCartPlough(input.contains("BH_CartPlow", Tag.TAG_COMPOUND)
                ? ItemStack.parse(self.registryAccess(), input.getCompound("BH_CartPlow")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY);
        BhHorseStorage.restoreUpgradedSaddle(inventory, input, self.registryAccess());
        bh_syncGearFlags();
        bh_afterLoad();

        if (input.contains("BH_GenderId", Tag.TAG_STRING)) {
            self.setData(BhHorseAttachments.GENDER, input.getString("BH_GenderId"));
        } else if (input.contains("BH_Gender", Tag.TAG_INT)) {
            self.setData(BhHorseAttachments.GENDER, (input.getInt("BH_Gender") == 0 ? BhContent.MALE : BhContent.FEMALE)
                    .getKey().location().toString());
        } else {
            self.setData(BhHorseAttachments.GENDER, (this.random.nextBoolean() ? BhContent.MALE : BhContent.FEMALE)
                    .getKey().location().toString());
        }
        if (input.contains("BH_BreedId", Tag.TAG_STRING)) {
            bh_readBreedId(input.getString("BH_BreedId"));
            self.setData(BhHorseAttachments.BREED_MIXED, input.getBoolean("BH_BreedMixed"));
        } else if (input.contains("BH_Breed", Tag.TAG_INT)) {
            bh_applyLegacyBreed(HorseBreed.fromId(input.getInt("BH_Breed")));
            self.setData(BhHorseAttachments.BREED_MIXED, input.getBoolean("BH_BreedMixed"));
        } else {
            bh_assignBreedPreservingCoat();
        }
        if (input.contains("BH_SpeciesId", Tag.TAG_STRING)) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, input.getString("BH_SpeciesId"));
        } else if (input.contains("BH_Species", Tag.TAG_INT)) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, bh_legacySpeciesKey(input.getInt("BH_Species")).location().toString());
        }

        bh_setLargeCart(input.contains("BH_CartLarge") ? input.getBoolean("BH_CartLarge") : this.bh_mayUseLargeCart());
        if (bh_ours()) bh_abilities.read(self, this, input.getCompound("BH_Abilities"));
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void bh_assignTraitsOnSpawn(ServerLevelAccessor level,
                                        DifficultyInstance difficulty,
                                        MobSpawnType reason,
                                        @Nullable SpawnGroupData groupData,
                                        CallbackInfoReturnable<SpawnGroupData> cir) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.GENDER, (this.random.nextBoolean() ? BhContent.MALE : BhContent.FEMALE)
                .getKey().location().toString());

        if ((Object) this instanceof BhBreedEntity breedEntity) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, breedEntity.bhFixedBreed().location().toString());
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhContent.SPECIES_NONE.getKey().location().toString());
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED_MIXED, false);
            return;
        }

        if (bh_getBreedKey() != null || !bh_getSpecies().equals(BhContent.SPECIES_NONE.getKey())) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseBreed species = HorseBreed.speciesFor(self);
        if (species != null) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhBreeds.speciesOf(species).location().toString());
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED_MIXED, false);
        }
    }

    @Unique
    private static ResourceKey<CommandType> bh_legacyCommandKey(int ordinal) {
        return switch (ordinal) {
            case 1 -> BhContent.COMMAND_STAY.getKey();
            case 2 -> BhContent.COMMAND_RETURN_HOME.getKey();
            case 3 -> BhContent.COMMAND_SET_HOME.getKey();
            case 4 -> BhContent.COMMAND_WANDER.getKey();
            case 5 -> BhContent.COMMAND_ABILITY.getKey();
            default -> BhContent.COMMAND_FOLLOW.getKey();
        };
    }

    @Unique
    private static ResourceKey<SpeciesType> bh_legacySpeciesKey(int ordinal) {
        return switch (ordinal) {
            case 1 -> BhContent.SPECIES_DONKEY.getKey();
            case 2 -> BhContent.SPECIES_MULE.getKey();
            case 3 -> BhContent.SPECIES_SKELETON.getKey();
            case 4 -> BhContent.SPECIES_ZOMBIE.getKey();
            default -> BhContent.SPECIES_NONE.getKey();
        };
    }

    @Unique
    private void bh_readBreedId(String raw) {
        if (raw.indexOf(':') < 0) {
            bh_applyLegacyBreed(HorseBreed.byId(raw));
            return;
        }
        ResourceLocation loc = ResourceLocation.tryParse(raw);
        if (loc != null && BhRegistries.breedTypeRegistry().containsKey(loc)) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, loc.toString());
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhContent.SPECIES_NONE.getKey().location().toString());
            return;
        }
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, "");
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhContent.SPECIES_NONE.getKey().location().toString());
    }

    @Unique
    private void bh_applyLegacyBreed(HorseBreed legacy) {
        ResourceKey<BreedType> key = BhBreeds.keyOf(legacy);
        if (key != null) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, key.location().toString());
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhContent.SPECIES_NONE.getKey().location().toString());
        } else {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED, "");
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SPECIES, BhBreeds.speciesOf(legacy).location().toString());
        }
    }

    @Unique
    private static void bh_writeBlockPos(CompoundTag tag, String key, BlockPos pos) {
        tag.putInt(key + "X", pos.getX());
        tag.putInt(key + "Y", pos.getY());
        tag.putInt(key + "Z", pos.getZ());
    }

    @Unique
    private void bh_assignBreedPreservingCoat() {
        HorseBreed picked = BhHorseTraits.pickBreed((AbstractHorse) (Object) this, this.random);
        bh_applyLegacyBreed(picked);
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.BREED_MIXED, false);
    }

    @Inject(method = "createInventory", at = @At("TAIL"))
    private void bh_onCreateInventory(CallbackInfo ci) {
        bh_afterInventoryChange();
        this.bh_syncGearFlags();
    }

    @Inject(method = "containerChanged", at = @At("TAIL"))
    private void bh_onContainerChanged(Container invBasic, CallbackInfo ci) {
        this.bh_syncGearFlags();
    }

    @Override
    public @Nullable UUID bh_getCombatTarget() {
        return this.bh_combatTarget;
    }

    @Override
    public void bh_setCombatTarget(@Nullable UUID target) {
        this.bh_combatTarget = target;
        this.bh_syncCombatState();
    }

    @Override
    public int bh_getSpookTicks() {
        return this.bh_spookTicks;
    }

    @Override
    public void bh_setSpookTicks(int ticks) {
        this.bh_spookTicks = Math.max(0, ticks);
        this.bh_syncCombatState();
    }

    @Override
    public @Nullable BreedAbility bh_currentAbility() {
        this.bh_abilities.initialize((AbstractHorse) (Object) this, this);
        return this.bh_abilities.current();
    }

    @Override
    public List<BreedAbility> bh_allAbilities() {
        this.bh_abilities.initialize((AbstractHorse) (Object) this, this);
        return this.bh_abilities.all();
    }

    @Override
    public List<ResourceKey<AbilityType>> bh_activeAbilities() {
        return this.bh_abilities.active((AbstractHorse) (Object) this, this);
    }

    @Override
    public boolean bh_activateAbility(ResourceKey<AbilityType> ability) {
        return this.bh_abilities.activate((AbstractHorse) (Object) this, this, ability);
    }

    @Override
    public boolean bh_isAbilityPaused() {
        return this.bh_abilityPaused;
    }

    @Override
    public void bh_setAbilityPaused(boolean paused) {
        this.bh_abilityPaused = paused;
    }

    @Override
    public int bh_getCombatState() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.COMBAT);
    }

    @Override
    public int bh_getKickTicks() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.KICK);
    }

    @Override
    public void bh_setKickTicks(int ticks) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.KICK, Math.max(0, ticks));
    }

    @Override
    public int bh_getJumpCue() {
        return this.bh_jumpCue;
    }

    @Override
    public void bh_cueJump() {
        this.bh_jumpCue++;
    }

    @Unique
    private void bh_syncCombatState() {
        int next = this.bh_spookTicks > 0 ? 2 : this.bh_combatTarget != null ? 1 : 0;
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.COMBAT) != next) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.COMBAT, next);
        }
    }

    @Override
    public int bh_getGear() {
        return ((AbstractHorse) (Object) this).level().isClientSide()
                ? ((AbstractHorse) (Object) this).getData(BhHorseAttachments.GEAR)
                : bh_gear;
    }

    @Override
    public void bh_setGear(int gear) {
        this.bh_gear = Mth.clamp(gear, 0, BhGears.TOP_GEAR);
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.GEAR, this.bh_gear);
    }

    @Override
    public int bh_getStompTicks() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.STOMP);
    }

    @Override
    public void bh_setStompTicks(int ticks) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.STOMP, Math.max(0, ticks));
    }

    @Override
    public int bh_getSurge() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.SURGE);
    }

    @Override
    public void bh_setSurge(int packed) {
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.SURGE) != packed) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.SURGE, packed);
        }
    }

    @Unique
    private static AttachmentType<Integer> bh_surgeKey(int slot) {
        return switch (slot) {
            case 0 -> BhHorseAttachments.SURGE;
            case 1 -> BhHorseAttachments.SURGE_1;
            case 2 -> BhHorseAttachments.SURGE_2;
            case 3 -> BhHorseAttachments.SURGE_3;
            default -> throw new IllegalArgumentException("Ability surge slot out of range: " + slot);
        };
    }

    @Override
    public int bh_getAbilitySurge(int slot) {
        return ((AbstractHorse) (Object) this).getData(bh_surgeKey(slot));
    }

    @Override
    public void bh_setAbilitySurge(int slot, int packed) {
        AttachmentType<Integer> key = bh_surgeKey(slot);
        if (((AbstractHorse) (Object) this).getData(key) != packed) {
            ((AbstractHorse) (Object) this).setData(key, packed);
        }
    }

    @Override
    public int bh_getPerkSurge() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.PERK);
    }

    @Override
    public void bh_setPerkSurge(int packed) {
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.PERK) != packed) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.PERK, packed);
        }
    }

    @Override
    public int bh_getPulse() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.PULSE);
    }

    @Override
    public void bh_setPulse(int packed) {
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.PULSE) != packed) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.PULSE, packed);
        }
    }

    @Override
    public int bh_getCharge() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CHARGE);
    }

    @Override
    public void bh_setCharge(int fill) {
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.CHARGE) != fill) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.CHARGE, fill);
        }
    }

    @Override
    public boolean bh_isFreeLook() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.FREE_LOOK);
    }

    @Override
    public void bh_setFreeLook(boolean freeLook) {
        if (((AbstractHorse) (Object) this).getData(BhHorseAttachments.FREE_LOOK) != freeLook) {
            ((AbstractHorse) (Object) this).setData(BhHorseAttachments.FREE_LOOK, freeLook);
        }
    }

    @Override
    public int bh_getGaitGear() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.GAIT_GEAR);
    }

    @Override
    public void bh_setGaitGear(int gear) {
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.GAIT_GEAR, Mth.clamp(gear, 0, BhGears.TOP_GEAR));
    }

    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    private void bh_applyGearSpeed(
            Player rider,
            CallbackInfoReturnable<Float> cir) {
        if (!bh_ours()) return;
        cir.setReturnValue(BhGears.riddenSpeed(bh_gear, cir.getReturnValueF()));
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bh_neighWhenHurt(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ()
                || !(self instanceof Horse)
                || self.getRandom().nextFloat() >= BH_HURT_NEIGH_CHANCE) {
            return;
        }
        self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
                ModSounds.HORSE_NEIGH, self.getSoundSource(), 1.0F, 1.0F);
    }


    @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 300))
    private int bh_grazeLessOften(int vanillaInterval) {
        return bh_ours() ? BH_GRAZE_ROLL_INTERVAL : vanillaInterval;
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "canEatGrass()Z"))
    private boolean bh_gateGrazing(AbstractHorse horse) {
        if (!horse.canEatGrass()) {
            return false;
        }
        if (!bh_ours()) {
            return true;
        }
        if (this.bh_mayGraze(horse)) {
            return true;
        }
        if (horse.isEating()) {
            horse.setEating(false);
            this.eatingCounter = 0;
        }
        return false;
    }

    @Unique
    private boolean bh_mayGraze(AbstractHorse horse) {
        if (horse.isVehicle() || horse.tickCount < this.bh_grazeBlockedUntilTick) {
            return false;
        }
        if (!this.bh_isOwned()) {
            return true;
        }
        ResourceKey<CommandType> command = this.bh_getCommand();
        return command.equals(BhContent.COMMAND_WANDER.getKey()) || command.equals(BhContent.COMMAND_STAY.getKey());
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bh_stopGrazingWhenHurt(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            AbstractHorse self = (AbstractHorse) (Object) this;
            this.bh_grazeBlockedUntilTick = self.tickCount + BH_GRAZE_HURT_COOLDOWN_TICKS;
            this.bh_combat.onHurt(self, this, source);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tick(CallbackInfo ci) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (BhVanillaHorseSwap.trySwap(self) || HorseTracker.discardIfStale(self)) {
            return;
        }
        for (HorseFeature feature : this.bh_features()) {
            feature.tick(self, this);
        }
    }

    @Unique
    private void bh_clearGearWhenUnridden(AbstractHorse self) {
        if (self.getControllingPassenger() == null) {
            if (bh_gear != 0) {
                bh_setGear(0);
                bh_setGaitGear(0);
            }
            bh_setFreeLook(false);
            for (int slot = 0; slot < BhSurge.ABILITY_SLOTS; slot++) {
                bh_setAbilitySurge(slot, 0);
            }
            bh_setPerkSurge(0);
            bh_setPulse(0);
            bh_setCharge(BhSurge.HIDDEN);
        }
    }

    @Inject(method = "fedFood", at = @At("HEAD"))
    private void bh_markGoldenAppleFeed(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir) {
        if (!bh_ours()) return;
        this.bh_fedGoldenAppleThisTick = stack.is(Items.GOLDEN_APPLE);
    }

    @Inject(method = "fedFood", at = @At("RETURN"))
    private void bh_rewardGoldenAppleBond(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir) {
        if (!bh_ours()) return;
        try {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (!this.bh_fedGoldenAppleThisTick || self.level().isClientSide() || !cir.getReturnValue().consumesAction()) {
                return;
            }

            BhHorseTraits.grantBond(this, 2);
        } finally {
            this.bh_fedGoldenAppleThisTick = false;
        }
    }

    @Inject(method = "doPlayerRide", at = @At("HEAD"), cancellable = true)
    private void bh_gateOwnerOnlyMount(Player player, CallbackInfo ci) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) return;
        if (this.bh_maySaddleUp(player.getUUID())) return;
        if (BhHorseInteraction.riderMayLeadPillion(self, this)) return;
        self.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.not_owner"));
        }
        if (player.getVehicle() == self) {
            player.stopRiding();
        }
        ci.cancel();
    }

    @Inject(
            method = "doPlayerRide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;setStanding(Z)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void bh_rotateHorseInsteadOfPlayer(Player player, CallbackInfo ci) {
        if (BhHorseInteraction.rotateHorseInsteadOfPlayer((AbstractHorse) (Object) this, this, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "tameWithName", at = @At("RETURN"))
    private void bh_claimHorseOnTame(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ() || self.level().isClientSide() || player.getUUID().equals(this.bh_getOwner())) {
            return;
        }

        this.bh_setOwner(player.getUUID());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.claimed"));
            BhCriteria.fire(serverPlayer, BhCriteria.OWN_HORSE);
            BhCriteria.fireBreed(serverPlayer, this.bh_getBreed());
            BhCriteria.fireOwnedHorseCount(serverPlayer);
        }
    }

    @Inject(method = "openCustomInventoryScreen", at = @At("HEAD"), cancellable = true)
    private void bh_blockNonOwnerInventoryAccess(Player player, CallbackInfo ci) {
        if (!bh_ours()) return;
        if (BhHorseInteraction.blockNonOwnerInventoryAccess((AbstractHorse) (Object) this, this, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_equipGearFromHand(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!bh_ours()) return;
        InteractionResult result = BhHorseInteraction.equipGearFromHand(
                (AbstractHorse) (Object) this, this, player, hand);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_allowSecondPlayerRider(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.isVehicle()
                || self.isBaby()
                || self.hasPassenger(player)
                || self.getPassengers().size() >= BhHorseSteering.bh_seatCount(this)) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (self.isTamed() && !heldItem.isEmpty() && self.isFood(heldItem)) {
            InteractionResult fed = self.fedFood(player, heldItem);
            if (fed.consumesAction()) {
                cir.setReturnValue(fed);
                return;
            }
        }

        InteractionResult animalResult = super.mobInteract(player, hand);
        if (animalResult.consumesAction()) {
            cir.setReturnValue(animalResult);
            return;
        }

        if (self.isTamed() && player.isSecondaryUseActive()) {
            self.openCustomInventoryScreen(player);
            cir.setReturnValue((self.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME));
            return;
        }

        if (!heldItem.isEmpty()) {
            InteractionResult heldItemResult = heldItem.interactLivingEntity(player, self, hand);
            if (heldItemResult.consumesAction()) {
                cir.setReturnValue(heldItemResult);
                return;
            }
        }

        if (!BhConfig.multiRidingEnabled()) {
            return;
        }

        this.doPlayerRide(player);
        cir.setReturnValue((self.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME));
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void bh_adjustFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseInteraction.StabilizerLanding landing =
                BhHorseInteraction.stabilizerLanding(self, this, distance);
        if (landing == BhHorseInteraction.StabilizerLanding.ABSORBED) {
            cir.setReturnValue(false);
            return;
        }

        double waiver = ArchetypePerks.fallDamageWaiver(BhBreedData.of(this.bh_getBreedKey()).archetype());
        if (waiver > 0.0D && distance < waiver) {
            if (distance > 1.0D) {
                self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
            }
            if (distance > self.getMaxFallDistance()) {
                BhSurge.pulsePerk(this, ArchetypePerks.FALL_BADGE);
            }
            cir.setReturnValue(false);
            return;
        }

        if (landing == BhHorseInteraction.StabilizerLanding.PASS_THROUGH) {
            return;
        }

        if (!this.bh_hasHoovesGear()) {
            return;
        }

        if (distance > 1.0D) {
            self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }

        int reducedDamage = this.calculateFallDamage(distance, damageMultiplier * BH_HOOVES_FALL_DAMAGE_MULTIPLIER);
        if (reducedDamage <= 0) {
            cir.setReturnValue(false);
            return;
        }

        self.hurt(source, reducedDamage);
        if (self.isVehicle()) {
            for (Entity passenger : self.getIndirectPassengers()) {
                passenger.hurt(source, reducedDamage);
            }
        }

        this.playBlockFallSound();
        cir.setReturnValue(true);
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void bh_dropGearAndChest(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;
        bh_dropCartChest();
        bh_dropCartPlough();
        BhHorseStorage.dropContainerContents(self, level, bh_gearContainer);
        BhHorseStorage.dropContainerContents(self, level, bh_chestContainer);
        bh_syncGearFlags();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bh_onRegisterGoals(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        goalSelector.addGoal(1, new SpookGoal(self));
        goalSelector.addGoal(2, new DefendOwnerGoal(self));
        goalSelector.addGoal(3, new HorseStayGoal(self));
        goalSelector.addGoal(3, new HorseFollowOwnerGoal(self));
        goalSelector.addGoal(3, new HorseReturnHomeGoal(self));
        goalSelector.addGoal(3, new HorseWanderBoundsGoal(self));
    }

    @Unique private Vec3 bh_rearSeatShift = Vec3.ZERO;

    @Redirect(
            method = "getPassengerAttachmentPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)"
                            + "Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 bh_noRearRiderShift(Vec3 attachment, Vec3 rearOffset) {
        if (!bh_ours()) return attachment.add(rearOffset);
        Vec3 applied = rearOffset.scale(BhRiderSeat.REAR_CAMERA_FOLLOW);
        this.bh_rearSeatShift = applied;
        return attachment.add(applied);
    }

    @Inject(method = "getPassengerAttachmentPoint", at = @At("RETURN"), cancellable = true)
    private void bh_offsetSecondPassenger(
            Entity passenger,
            EntityDimensions dimensions,
            float scaleFactor,
            CallbackInfoReturnable<Vec3> cir) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;

        if (this.bh_hasCartGear()) {
            if (self.level().isClientSide()) {
                BhRiderSeat.publish(self.getId(), Vec3.ZERO);
            }
            cir.setReturnValue(HorseCartEntity
                    .benchSeatOffset(self, BhHorseSteering.benchSeatIndex(self, passenger), self.yBodyRot));
            return;
        }

        Vec3 lift = new Vec3(0.0D, BhRiderSeat.seatLift(self), 0.0D);
        BhRiderSeat.publish(self.getId(), this.bh_rearSeatShift.add(lift));
        if (lift.y != 0.0D) {
            cir.setReturnValue(cir.getReturnValue().add(lift));
        }

        double drop = BhRiderSeat.seatDrop(passenger);
        if (drop != 0.0D) {
            cir.setReturnValue(cir.getReturnValue().subtract(0.0D, drop, 0.0D));
        }

        Vec3 offset = BhHorseSteering.multiRiderOffset(self, passenger);
        if (offset != null) {
            cir.setReturnValue(cir.getReturnValue().add(offset));
        }
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void bh_allowMountedFreeCamera(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!bh_ours()) return;
        if (!(rider instanceof Player player)) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;

        Vec2 rotation = BhHorseSteering.riddenRotation(self, this, player);
        if (rotation != null) {
            cir.setReturnValue(rotation);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return BhHorseSteering.canAddPassenger((AbstractHorse) (Object) this, this, passenger);
    }

    @Override
    public ProjectileDeflection deflection(Projectile projectile) {
        if (!Ironclad.deflectsProjectiles(this)) {
            return super.deflection(projectile);
        }
        if (!level().isClientSide()) {
            BhSurge.pulse(this, 0, 1);
        }
        return ProjectileDeflection.REVERSE;
    }

    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void bh_keepPlayerAtTheReins(CallbackInfoReturnable<LivingEntity> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (cir.getReturnValue() != null
                || !this.bh_hasCartGear()
                || !self.isSaddled()) {
            return;
        }

        for (Entity passenger : self.getPassengers()) {
            if (passenger instanceof Player player) {
                cir.setReturnValue(player);
                return;
            }
        }
    }

    @Unique
    private void bh_applyBondAttributes() {
        BhHorseTraits.applyBondAttributes((AbstractHorse) (Object) this, bh_bond);
    }

    @Unique
    private boolean bh_hasHoovesGear() {
        return BhConfig.hoovesEnabled() && this.bh_hasGear(GearSlot.HOOVES);
    }

    @Unique
    private void bh_dropChestContents() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_chestContainer);
    }

    @Unique
    private void bh_syncGearFlags() {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            return;
        }
        int flags = 0;
        for (GearSlot slot : GearSlot.values()) {
            if (slot.accepts(this.bh_gearContainer.getItem(slot.ordinal()))
                    && !this.bh_gearContainer.getItem(slot.ordinal()).isEmpty()) {
                flags |= 1 << slot.ordinal();
            }
        }

        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.GEAR_FLAGS, flags);
        boolean hadCart = ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART);
        boolean hasCart = this.bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal()).is(ModItems.HORSE_CART);
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.CART, hasCart);
        if (hasCart && !hadCart) {
            bh_setLargeCart(this.bh_mayUseLargeCart());
        }
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.ENDER_CHEST,
                this.bh_gearContainer.getItem(GearSlot.CHEST.ordinal()).is(Items.ENDER_CHEST));
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.UPGRADED_SADDLE,
                this.inventory != null && this.inventory.getItem(0).is(ModItems.UPGRADED_SADDLE));
        this.bh_syncStabilizerCharge();
    }

    @Override
    public float bh_getStabilizerCharge() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.STABILIZER_CHARGE) / 1000.0F;
    }

    @Override
    public void bh_syncStabilizerCharge() {
        ItemStack harness = this.bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal());
        int charge = 0;
        if (harness.is(ModItems.HORSE_STABILIZER)) {
            int usable = harness.getMaxDamage() - 1;
            charge = usable <= 0 ? 0 : Math.max(0, usable - harness.getDamageValue()) * 1000 / usable;
        }
        ((AbstractHorse) (Object) this).setData(BhHorseAttachments.STABILIZER_CHARGE, charge);
    }

    @Override
    public boolean bh_hasEnderChestGear() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.ENDER_CHEST);
    }

    @Override
    public boolean bh_hasCartGear() {
        return ((AbstractHorse) (Object) this).getData(BhHorseAttachments.CART);
    }

    @Override
    public void bh_ridePlayer(Player player) {
        this.doPlayerRide(player);
    }

    @Override
    public void bh_clearStanding() {
        ((AbstractHorse) (Object) this).setStanding(false);
        this.standCounter = 0;
    }

    @Redirect(
            method = "handleStartJump(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "standIfPossible()V"))
    private void bh_noRearOnStartJump(AbstractHorse horse) {
        if (!bh_ours()) horse.standIfPossible();
    }

    @Inject(method = "standIfPossible", at = @At("HEAD"), cancellable = true)
    private void bh_noRearInMidair(CallbackInfo ci) {
        if (!bh_ours()) return;
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.onGround()
                || (self.hurtTime > 0
                    && ArchetypePerks.suppressesRear(BhBreedData.of(this.bh_getBreedKey()).archetype()))) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "onPlayerJump(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "standIfPossible()V"))
    private void bh_noRearOnPlayerJump(AbstractHorse horse) {
        if (!bh_ours()) horse.standIfPossible();
    }

    @Inject(method = "executeRidersJump", at = @At("TAIL"))
    private void bh_cueRiderJump(float scale, Vec3 input, CallbackInfo ci) {
        if (bh_ours() && ((AbstractHorse) (Object) this).level().isClientSide()) {
            this.bh_jumpCue++;
        }
    }

    @Inject(method = "handleStartJump", at = @At("TAIL"))
    private void bh_shareJump(int power, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!bh_ours() || self.level().isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingEntity(self, new HorseJumpPayload(self.getId()));
    }

    @Unique
    private void bh_afterLoad() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onLoad(self, this);
        }
    }

    @Unique
    private void bh_afterInventoryChange() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onInventoryChanged(self, this);
        }
    }
}
