package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
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

    private static final Tune BRACE_KNOCKBACK = Tune.of("mammoth_worker", "brace_knockback_resistance", 1.0D).within(0.0D, 1.0D);
    private static final Tune BRACE_TAKEN = Tune.of("mammoth_worker", "brace_damage_taken", 0.8D).within(0.0D, 10.0D);
    private static final Tune HASTE_RADIUS = Tune.of("mammoth_worker", "haste_radius", 12.0D).within(0.0D, 128.0D);
    private static final Tune HASTE_LEVEL = Tune.of("mammoth_worker", "haste_level", 1.0D).within(1.0D, 10.0D);

    private boolean anchored;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        boolean want = braced(horse);
        if (want != anchored) {
            anchored = want;
            BhHorseAttributes.apply(horse, Attributes.KNOCKBACK_RESISTANCE,
                    BhHorseAttributes.Source.ABILITY, "mammoth_brace", want ? BRACE_KNOCKBACK.get() : 0.0D,
                    AttributeModifier.Operation.ADD_VALUE);
            if (want) {
                BhSurge.pulse(data, 0, 2);
            }
        }
        if (horse.tickCount % 10 == 0 && BhAbility.MAMMOTH_RUBBLE.on(data)) {
            DonkeyForaging.pickup(horse, data, MammothWorker::rubble);
        }
        if (BhAbility.MAMMOTH_WORK.tier(data) >= 1 && horse.tickCount % 20 == 0) {
            double r = HASTE_RADIUS.get();
            for (Player player : horse.level().getEntitiesOfClass(Player.class,
                    horse.getBoundingBox().inflate(r), p -> p.isAlive() && horse.distanceToSqr(p) <= r * r)) {
                if (!player.hasEffect(MobEffects.DIG_SPEED)) {
                    BhSurge.pulse(data, 0, 1);
                }
                BhBreedAbilities.applyQuietEffect(player, MobEffects.DIG_SPEED, 40, HASTE_LEVEL.ticks() - 1);
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
        return horse != null
                && BhAbility.MAMMOTH_BRACE.tier(IHorseData.of(horse), BhContent.MAMMOTH_DONKEY.getKey()) >= 2
                && BhBreedAbilities.rider(horse) != null;
    }

    public static float resistanceScale(LivingEntity target) {
        return braced(target) ? BRACE_TAKEN.f() : 1.0F;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        anchored = false;
        BhHorseAttributes.clear(horse, Attributes.KNOCKBACK_RESISTANCE,
                BhHorseAttributes.Source.ABILITY, "mammoth_brace");
    }
}
