package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.Block;

public final class ArchetypePerks {

    private static final TagKey<Block> ROAD = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_road"));

    private static final String PATH_KEY = "path";
    private static final int PATH_INTERVAL = 10;
    private static final int PATH_GRACE = 40;

    private double pathBonus = -1.0D;
    private int pathGrace;

    public void onBreedChanged(AbstractHorse horse, BreedArchetype arch) {
        BhHorseAttributes.apply(horse, Attributes.KNOCKBACK_RESISTANCE,
                BhHorseAttributes.Source.ARCHETYPE, "mass",
                arch.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE);
    }

    public void tick(AbstractHorse horse, IHorseData data, BreedArchetype arch) {
        int heal = arch.passiveHealInterval();
        if (heal > 0 && horse.tickCount % heal == 0 && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(1.0F);
        }

        if (horse.tickCount % PATH_INTERVAL == 0) {
            updatePath(horse, data, arch);
        }
    }

    private void updatePath(AbstractHorse horse, IHorseData data, BreedArchetype arch) {
        if (BhBreedAbilities.rider(horse) == null) {
            pathGrace = 0;
        } else if (horse.getBlockStateOn().is(ROAD)) {
            pathGrace = PATH_GRACE;
        } else if (pathGrace > 0) {
            pathGrace = Math.max(0, pathGrace - PATH_INTERVAL);
        }

        double want = pathGrace > 0
                ? arch.pathSpeedBonus(BhHorseTraits.bondTier(data.bh_getBond()))
                : 0.0D;
        if (want == pathBonus) {
            return;
        }
        pathBonus = want;
        data.bh_setPerkSurge(want <= 0.0D ? 0 : BhSurge.pack(BhSurge.ACTIVE, 0, 0,
                (int) Math.round(want * 100.0D)));
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ARCHETYPE, PATH_KEY, want,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public void clear(AbstractHorse horse) {
        pathBonus = -1.0D;
        pathGrace = 0;
        IHorseData.of(horse).bh_setPerkSurge(0);
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ARCHETYPE, PATH_KEY);
    }
}
