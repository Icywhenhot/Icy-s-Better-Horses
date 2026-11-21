package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class ArchetypePerks {

    private static final TagKey<Block> ROAD = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "horse_road"));

    private static final String PATH_KEY = "path";
    private static final int PATH_INTERVAL = 10;
    private static final int PATH_GRACE = 40;
    private static final double DEFAULT_STEP_HEIGHT = 1.125D;
    private static final int GUARD_INTERVAL = 20;
    public static final int MEDKIT_BADGE = 1;
    public static final Tune MEDKIT_THRESHOLD = Tune.of("medkit", "use_below_health", 0.5D).within(0.0D, 1.0D);
    private static final Tune MEDKIT_TICKS = Tune.of("medkit", "effect_ticks", 600.0D).within(1.0D, 72000.0D);
    private static final int SNOW_BADGE = 2;
    public static final int FALL_BADGE = 3;

    private double pathBonus = -1.0D;
    private int graceUntil;
    private double mass = Double.NaN;
    private double armor = Double.NaN;
    private double step = Double.NaN;

    public void onBreedChanged(AbstractHorse horse, ArchetypeType arch) {
        BhHorseAttributes.apply(horse, Attributes.KNOCKBACK_RESISTANCE,
                BhHorseAttributes.Source.ARCHETYPE, "mass",
                knockbackResistance(arch), AttributeModifier.Operation.ADD_VALUE);
        double mult = arch.armorMultiplier();
        BhHorseAttributes.apply(horse, Attributes.ARMOR, BhHorseAttributes.Source.ARCHETYPE, "armor",
                mult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        BhHorseAttributes.apply(horse, Attributes.ARMOR_TOUGHNESS, BhHorseAttributes.Source.ARCHETYPE, "armor",
                mult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public void tick(AbstractHorse horse, IHorseData data, ArchetypeType arch) {
        double wantedMass = knockbackResistance(arch);
        if (wantedMass != mass || arch.armorMultiplier() != armor) {
            mass = wantedMass;
            armor = arch.armorMultiplier();
            onBreedChanged(horse, arch);
        }
        double wantedStep = stepHeight(arch);
        if (wantedStep != step) {
            step = wantedStep;
            horse.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(step);
        }
        int heal = passiveHealInterval(arch);
        if (heal > 0 && horse.tickCount % heal == 0 && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(1.0F);
        }

        if (horse.tickCount % PATH_INTERVAL == 0) {
            updatePath(horse, data, arch);
        }
        if (arch == BhContent.DONKEY.get() && horse.tickCount % GUARD_INTERVAL == 0 && BhAbility.DONKEY_GUARD.on()) {
            Guardian.hunt(horse, data);
        }
        if (arch == BhContent.WILD.get()) {
            WildTraits.tick(horse, data);
        }
    }

    private void updatePath(AbstractHorse horse, IHorseData data, ArchetypeType arch) {
        if (BhBreedAbilities.rider(horse) == null) {
            graceUntil = 0;
        } else if (horse.getBlockStateOn().is(ROAD)) {
            graceUntil = horse.tickCount + PATH_GRACE;
        }

        double want = horse.tickCount < graceUntil
                ? pathSpeedBonus(arch, BhHorseTraits.bondTier(data.bh_getBond()))
                : 0.0D;
        if (want != pathBonus) {
            pathBonus = want;
            BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ARCHETYPE, PATH_KEY, want,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
        if (BhSurge.phase(data.bh_getPerkSurge()) == BhSurge.PULSE) {
            return;
        }
        if (walksOnPowderSnow(arch) && horse.getBlockStateOn().is(Blocks.POWDER_SNOW)) {
            data.bh_setPerkSurge(BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, SNOW_BADGE));
            return;
        }
        data.bh_setPerkSurge(want <= 0.0D ? 0 : BhSurge.pack(BhSurge.ACTIVE, 0, 0,
                (int) Math.round(want * 100.0D)));
    }

    public void clear(AbstractHorse horse) {
        pathBonus = -1.0D;
        graceUntil = 0;
        IHorseData.of(horse).bh_setPerkSurge(0);
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ARCHETYPE, PATH_KEY);
    }

    public static double knockbackResistance(ArchetypeType arch) {
        if (arch == BhContent.DRAFT.get() && !BhAbility.DRAFT_MASS.on()) {
            return 0.0D;
        }
        return arch.knockbackResistance();
    }

    public static double stepHeight(ArchetypeType arch) {
        if (arch == BhContent.PONY.get() && !BhAbility.PONY_STEP.on()) {
            return DEFAULT_STEP_HEIGHT;
        }
        return arch.stepHeight();
    }

    public static int passiveHealInterval(ArchetypeType arch) {
        if (arch == BhContent.PONY.get() && !BhAbility.PONY_HEAL.on()) {
            return 0;
        }
        return arch.passiveHealInterval();
    }

    public static double pathSpeedBonus(ArchetypeType arch, int tier) {
        if (arch == BhContent.WESTERN.get() && !BhAbility.WESTERN_ROAD.on()) {
            return 0.0D;
        }
        return arch.pathSpeedBonus(tier);
    }

    public static boolean walksOnPowderSnow(ArchetypeType arch) {
        if (arch == BhContent.PONY.get() && !BhAbility.PONY_SNOW.on()) {
            return false;
        }
        return arch.walksOnPowderSnow();
    }

    public static boolean suppressesRear(ArchetypeType arch) {
        if (arch == BhContent.WAR.get() && !BhAbility.WAR_STEADY.on()) {
            return false;
        }
        return arch.suppressesRear();
    }

    public static int medkitMultiplier(ArchetypeType arch) {
        if (arch == BhContent.WAR.get() && !BhAbility.WAR_MEDKIT.on()) {
            return 1;
        }
        return arch.medkitMultiplier();
    }

    public static double fallDamageWaiver(ArchetypeType arch) {
        if (arch == BhContent.PONY.get() && !BhAbility.PONY_FALL.on()
                || arch == BhContent.MULE.get() && !BhAbility.MULE_SURE_FOOT.on()) {
            return 0.0D;
        }
        return arch.fallDamageWaiver();
    }

    public static double spookChance(ArchetypeType arch, int tier) {
        if (arch == BhContent.WAR.get() && BhAbility.WAR_STEADY.on()) {
            return 0.0D;
        }
        return arch.spookChance(tier);
    }

    public static boolean hasMedkit(IHorseData data) {
        return BhConfig.medkitEnabled()
                && data.bh_getGearContainer().getItem(GearSlot.MEDKIT.ordinal()).is(ModItems.HORSE_MEDKIT);
    }

    public static List<MobEffectInstance> medkitEffects(int duration) {
        return List.of(new MobEffectInstance(MobEffects.REGENERATION, duration, 0),
                new MobEffectInstance(MobEffects.HEAL, 1, 0),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
    }

    public static void useMedkit(LivingEntity self, IHorseData data) {
        if (self.level().isClientSide() || !hasMedkit(data)) {
            return;
        }
        SimpleContainer gear = data.bh_getGearContainer();
        gear.setItem(GearSlot.MEDKIT.ordinal(), ItemStack.EMPTY);
        gear.setChanged();
        int duration = MEDKIT_TICKS.ticks() * medkitMultiplier(BhBreedData.of(data.bh_getBreedKey()).archetype());
        for (MobEffectInstance effect : medkitEffects(duration)) {
            self.addEffect(effect);
        }
        BhSurge.pulsePerk(data, MEDKIT_BADGE);
        if (self instanceof AbstractHorse horse) {
            PegaCompanion.medkitUsed(horse, data, duration);
        }
    }
}
