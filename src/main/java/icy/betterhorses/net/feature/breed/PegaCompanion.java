package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class PegaCompanion implements BreedAbility {

    private static final String KEY = "pega_companion";
    private static final float REFUND_CHANCE = 0.4F;
    private static final int RALLY_TICKS = 20 * 15;
    private static final double RALLY_SPEED = 0.25D;

    private final Set<Holder<MobEffect>> ailments = new HashSet<>();
    private int kitWait;
    private int rally;
    private boolean fast;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide()) {
            return;
        }
        if (kitWait > 0 && --kitWait == 0 && horse.isAlive()
                && horse.getHealth() < horse.getMaxHealth() * ArchetypePerks.MEDKIT_THRESHOLD) {
            ArchetypePerks.useMedkit(horse, data);
        }
        boolean stirred = freshAilment(horse) || horse.hurtTime > 0 || SlowBlockImmunity.wading(horse);
        if (stirred && BhAbility.PEGA_RALLY.tier(data, BhContent.PEGA_DONKEY.key()) >= 2) {
            if (rally == 0) {
                BhSurge.pulse(data, 25, 3);
            }
            rally = RALLY_TICKS;
        } else if (rally > 0) {
            rally--;
        }
        if (fast != rally > 0) {
            fast = rally > 0;
            if (fast) {
                BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED, BhHorseAttributes.Source.ABILITY, KEY,
                        RALLY_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            } else {
                BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED, BhHorseAttributes.Source.ABILITY, KEY);
            }
        }
    }

    private boolean freshAilment(AbstractHorse horse) {
        boolean fresh = false;
        Set<Holder<MobEffect>> now = new HashSet<>();
        for (MobEffectInstance effect : horse.getActiveEffects()) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                now.add(effect.getEffect());
                fresh |= !ailments.contains(effect.getEffect());
            }
        }
        ailments.clear();
        ailments.addAll(now);
        return fresh;
    }

    public static void medkitUsed(AbstractHorse horse, IHorseData data, int duration) {
        int tier = BhAbility.PEGA_MEDKIT.tier(data, BhContent.PEGA_DONKEY.key());
        Player rider = BhBreedAbilities.rider(horse);
        if (rider != null && BhAbility.PEGA_SHARE.tier(data, BhContent.PEGA_DONKEY.key()) >= 1) {
            for (MobEffectInstance effect : ArchetypePerks.medkitEffects(duration)) {
                rider.addEffect(effect, horse);
            }
            BhSurge.pulse(data, 0, 2);
        }
        PegaCompanion pega = of(horse);
        if (tier < 0 || pega == null || horse.getRandom().nextFloat() >= REFUND_CHANCE) {
            return;
        }
        SimpleContainer gear = data.bh_getGearContainer();
        gear.setItem(GearSlot.MEDKIT.ordinal(), new ItemStack(ModItems.HORSE_MEDKIT));
        gear.setChanged();
        pega.kitWait = duration;
        BhSurge.pulse(data, 0, 1);
    }

    public static boolean holdingKit(Entity entity) {
        PegaCompanion pega = of(entity);
        return pega != null && pega.kitWait > 0;
    }

    private static @Nullable PegaCompanion of(Entity entity) {
        if (entity instanceof IHorseAbilityHost host) {
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof PegaCompanion pega) {
                    return pega;
                }
            }
        }
        return null;
    }

    public static float exhaustion(Player player, float amount) {
        return player.getVehicle() instanceof AbstractHorse horse
                && BhAbility.PEGA_SEAT.tier(IHorseData.of(horse), BhContent.PEGA_DONKEY.key()) >= 0
                ? amount * 0.75F : amount;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        kitWait = 0;
        rally = 0;
        fast = false;
        ailments.clear();
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED, BhHorseAttributes.Source.ABILITY, KEY);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("KitWait", kitWait);
        tag.putInt("Rally", rally);
    }

    @Override
    public void load(CompoundTag tag) {
        kitWait = tag.getIntOr("KitWait", 0);
        rally = tag.getIntOr("Rally", 0);
        fast = false;
    }
}
