package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public final class MammothWorker implements BreedAbility {

    private boolean anchored;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        boolean want = braced(horse);
        if (want != anchored) {
            anchored = want;
            BhHorseAttributes.apply(horse, Attributes.KNOCKBACK_RESISTANCE,
                    BhHorseAttributes.Source.ABILITY, "mammoth_brace", want ? 1.0D : 0.0D,
                    AttributeModifier.Operation.ADD_VALUE);
            if (want) {
                BhSurge.pulse(data, 0, 2);
            }
        }
        if (horse.tickCount % 10 == 0 && BhAbility.MAMMOTH_RUBBLE.on()) {
            DonkeyForaging.pickup(horse, data, MammothWorker::rubble);
        }
        if (tier >= 1 && BhAbility.MAMMOTH_WORK.on() && horse.tickCount % 20 == 0) {
            for (Player player : horse.level().getEntitiesOfClass(Player.class,
                    horse.getBoundingBox().inflate(12.0D), p -> p.isAlive() && horse.distanceToSqr(p) <= 144.0D)) {
                if (!player.hasEffect(MobEffects.DIG_SPEED)) {
                    BhSurge.pulse(data, 0, 1);
                }
                BhBreedAbilities.applyQuietEffect(player, MobEffects.DIG_SPEED, 40, 0);
            }
        }
    }

    private static boolean rubble(ItemStack stack) {
        return stack.is(Tags.Items.STONES) || stack.is(Tags.Items.COBBLESTONES)
                || stack.is(Tags.Items.ORES) || stack.is(Tags.Items.RAW_MATERIALS)
                || stack.is(Tags.Items.GEMS) || stack.is(Tags.Items.DUSTS_REDSTONE)
                || stack.is(Items.COAL) || stack.is(Items.FLINT);
    }

    public static boolean braced(LivingEntity target) {
        AbstractHorse horse = target instanceof AbstractHorse h ? h
                : target.getVehicle() instanceof AbstractHorse h ? h : null;
        return horse != null && BhAbility.MAMMOTH_BRACE.on()
                && BhContent.MAMMOTH_DONKEY.getKey().equals(IHorseData.of(horse).bh_getBreedKey())
                && BhHorseTraits.bondTier(IHorseData.of(horse).bh_getBond()) >= 2
                && BhBreedAbilities.rider(horse) != null;
    }

    public static float resistanceScale(LivingEntity target) {
        return braced(target) ? 0.8F : 1.0F;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        anchored = false;
        BhHorseAttributes.clear(horse, Attributes.KNOCKBACK_RESISTANCE,
                BhHorseAttributes.Source.ABILITY, "mammoth_brace");
    }
}
