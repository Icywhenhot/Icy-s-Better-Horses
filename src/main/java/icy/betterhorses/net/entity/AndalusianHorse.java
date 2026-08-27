package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class AndalusianHorse extends MediumHorse {

    private static final int COURAGE_TICKS = 3 * 20;

    private boolean takingDamage;

    public AndalusianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.ANDALUSIAN;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.ANDALUSIAN;
    }

    @Override
    public void standIfPossible() {
        if (takingDamage) {
            return;
        }
        super.standIfPossible();
    }

    @Override
    public boolean hurtServer(
            ServerLevel level,
            DamageSource source,
            float amount) {
        boolean hurt;
        takingDamage = true;
        try {
            hurt = super.hurtServer(level, source, amount);
        } finally {
            takingDamage = false;
        }

        if (hurt) {
            BhBreedAbilities.grantResistance(this, COURAGE_TICKS);
            Player rider = BhBreedAbilities.rider(this);
            if (rider != null) {
                BhBreedAbilities.grantResistance(rider, COURAGE_TICKS);
            }
        }
        return hurt;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.JUMP_STRENGTH, 0.72D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
