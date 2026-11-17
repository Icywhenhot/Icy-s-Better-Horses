package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
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
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "snow_trail"));

    private static final Tune SNOW_BONUS = Tune.of("winter_feet", "snow_speed_bonus", 0.0D, 0.2D, 0.5D).within(0.0D, 10.0D);
    private static final Tune ICE_FRICTION = Tune.of("winter_feet", "ice_friction", 0.6D).within(0.0D, 1.0D);

    private double bonus = -1.0D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhAbility.ZAMORANO_SNOW.tier(data);
        double want = tier >= 0 && horse.onGround()
                && (snow(horse.getBlockStateOn()) || snow(horse.level().getBlockState(horse.blockPosition())))
                ? SNOW_BONUS.at(tier) : 0.0D;
        if (want != bonus) {
            bonus = want;
            BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ABILITY, "snow_trail", want,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
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
        return target instanceof AbstractHorse horse
                && BhAbility.ZAMORANO_ICE.tier(IHorseData.of(horse), BhContent.ZAMORANO_DONKEY.getKey()) >= 0
                && horse.level().getBlockState(horse.getOnPos()).is(BlockTags.ICE)
                ? ICE_FRICTION.f() : original;
    }
}
