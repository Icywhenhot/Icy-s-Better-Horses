package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class WinterFeet implements BreedAbility {

    private static final TagKey<Block> SNOW = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "snow_trail"));

    private double bonus = -1.0D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        double want = tier >= 1 && BhAbility.ZAMORANO_SNOW.on() && horse.onGround()
                && (snow(horse.getBlockStateOn()) || snow(horse.level().getBlockState(horse.blockPosition())))
                ? tier >= 2 ? 0.5D : 0.2D : 0.0D;
        if (want != bonus) {
            bonus = want;
            BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ABILITY, "snow_trail", want,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
        data.bh_setSurge(want > 0.0D ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, (int) (want * 100.0D), 1) : 0);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        bonus = -1.0D;
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, "snow_trail");
        data.bh_setSurge(0);
    }

    private static boolean snow(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)
                || state.is(SNOW);
    }

    public static float friction(LivingEntity target, float original) {
        return target instanceof AbstractHorse horse && BhAbility.ZAMORANO_ICE.on()
                && BhContent.ZAMORANO_DONKEY.getKey().equals(IHorseData.of(horse).bh_getBreedKey())
                && horse.level().getBlockState(horse.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.ICE)
                ? 0.6F : original;
    }
}
