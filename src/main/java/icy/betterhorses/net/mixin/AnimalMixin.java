package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin {

    /**
     * Vanilla {@code checkAnimalSpawnRules} only allows spawning on blocks in
     * {@code BlockTags.ANIMALS_SPAWNABLE_ON} (grass blocks). That blocks horses
     * from ever spawning in deserts, snowy plains, badlands etc., even after we
     * add a spawn entry to those biomes. Relax the ground check for horse-like
     * entities specifically — sand, snow, dirt, gravel, terracotta, ice and
     * stone all count as valid horse ground.
     */
    @Inject(method = "checkAnimalSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void bh_relaxHorseGroundCheck(EntityType<? extends Animal> type,
                                                 LevelAccessor level,
                                                 EntitySpawnReason reason,
                                                 BlockPos pos,
                                                 RandomSource random,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!AbstractHorse.class.isAssignableFrom(type.getBaseClass())) return;

        // Inline brightness check (vanilla isBrightEnoughToSpawn is protected so we can't call it from here):
        // animal spawning normally requires raw brightness > 8.
        boolean brightEnough = EntitySpawnReason.ignoresLightRequirements(reason)
                || level.getRawBrightness(pos, 0) > 8;
        if (!brightEnough) {
            cir.setReturnValue(false);
            return;
        }

        BlockState below = level.getBlockState(pos.below());
        boolean groundOk = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(BlockTags.SAND)
                || below.is(BlockTags.DIRT)
                || below.is(BlockTags.TERRACOTTA)
                || below.is(BlockTags.SNOW)
                || below.is(Blocks.GRAVEL)
                || below.is(Blocks.PACKED_ICE)
                || below.is(Blocks.ICE)
                || below.is(Blocks.STONE)
                || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.POWDER_SNOW);
        cir.setReturnValue(groundOk);
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void bh_blockSameGenderBreeding(Animal other, CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (!(self instanceof AbstractHorse selfHorse) || !(other instanceof AbstractHorse otherHorse)) {
            return;
        }
        HorseGender selfGender = ((IHorseData) selfHorse).bh_getGender();
        HorseGender otherGender = ((IHorseData) otherHorse).bh_getGender();
        if (selfGender == otherGender) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void bh_finalizeHorseChild(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (!(self instanceof AbstractHorse selfHorse)
                || !(partner instanceof AbstractHorse partnerHorse)
                || !(child instanceof AbstractHorse childHorse)) {
            return;
        }

        IHorseData selfData = (IHorseData) selfHorse;
        IHorseData partnerData = (IHorseData) partnerHorse;
        IHorseData childData = (IHorseData) childHorse;

        // Gender: random for the child.
        childData.bh_setGender(self.getRandom().nextBoolean() ? HorseGender.MALE : HorseGender.FEMALE);

        // Breed inheritance — only meaningful if both parents are real horses (not donkey/mule mixes).
        HorseBreed selfBreed = selfData.bh_getBreed();
        HorseBreed partnerBreed = partnerData.bh_getBreed();
        if (selfBreed.isRealBreed() && partnerBreed.isRealBreed()) {
            if (selfBreed == partnerBreed) {
                childData.bh_setBreed(selfBreed);
                childData.bh_setMixedBreed(false);
            } else {
                HorseBreed chosen = self.getRandom().nextBoolean() ? selfBreed : partnerBreed;
                childData.bh_setBreed(chosen);
                childData.bh_setMixedBreed(true);
            }
        } else {
            // Cross-species (e.g. horse + donkey -> mule). Use the placeholder for the child's species.
            HorseBreed species = HorseBreed.speciesFor(childHorse);
            childData.bh_setBreed(species != null ? species : HorseBreed.UNKNOWN_SPECIES);
            childData.bh_setMixedBreed(false);
        }

        // Stat inheritance: child = max(parent1, parent2) + random delta in display units
        // (blocks/sec for speed, blocks for jump, HP for health), clamped at the vanilla horse
        // base ceiling so bond's +75% multiplier still tops out at the mod-attainable max.
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MAX_HEALTH, VANILLA_MAX_HEALTH, HEALTH_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.MOVEMENT_SPEED, VANILLA_MAX_SPEED, SPEED_DISPLAY_PER_RAW);
        bh_inheritBetterStat(selfHorse, partnerHorse, childHorse, Attributes.JUMP_STRENGTH, VANILLA_MAX_JUMP, JUMP_DISPLAY_PER_RAW);

        // Ensure max-health change actually heals the child to its new ceiling.
        childHorse.setHealth(childHorse.getMaxHealth());

        // Coat follows breed: roll a coat from the child's assigned breed's allowed list.
        if (childHorse instanceof Horse childHorseEntity) {
            HorseBreed childBreed = childData.bh_getBreed();
            HorseBreed.Coat coat = childBreed.rollCoat(self.getRandom());
            if (coat != null) {
                ((HorseAccessor) childHorseEntity).bh_setVariantAndMarkings(coat.color(), coat.markings());
            }
        }
    }

    // Vanilla horse base attribute ceilings — these are the caps breeding can never exceed.
    private static final double VANILLA_MAX_HEALTH = 30.0D;
    private static final double VANILLA_MAX_SPEED = 0.3375D;
    private static final double VANILLA_MAX_JUMP = 1.0D;

    // display_value = raw * factor. Used to convert the -0.5..+1.0 delta from display units back to raw.
    // Speed: blocks/sec = raw * 43.2. Jump: blocks = raw * 6 - 1 (slope is 6). Health: HP = raw * 1.
    private static final double SPEED_DISPLAY_PER_RAW = 43.2D;
    private static final double JUMP_DISPLAY_PER_RAW = 6.0D;
    private static final double HEALTH_DISPLAY_PER_RAW = 1.0D;

    // Variance is uniform in display units: at worst 0.5 worse than the better parent, at best 1.0 better.
    private static final double VARIANCE_DISPLAY_MIN = -0.5D;
    private static final double VARIANCE_DISPLAY_MAX = 1.0D;

    private static void bh_inheritBetterStat(AbstractHorse p1, AbstractHorse p2, AbstractHorse child,
                                             Holder<Attribute> attr, double cap, double displayPerRaw) {
        AttributeInstance p1Attr = p1.getAttribute(attr);
        AttributeInstance p2Attr = p2.getAttribute(attr);
        AttributeInstance childAttr = child.getAttribute(attr);
        if (p1Attr == null || p2Attr == null || childAttr == null) return;

        double best = Math.max(p1Attr.getBaseValue(), p2Attr.getBaseValue());
        double deltaDisplay = VARIANCE_DISPLAY_MIN
                + p1.getRandom().nextDouble() * (VARIANCE_DISPLAY_MAX - VARIANCE_DISPLAY_MIN);
        double deltaRaw = deltaDisplay / displayPerRaw;
        childAttr.setBaseValue(Math.max(0.0D, Math.min(cap, best + deltaRaw)));
    }
}
