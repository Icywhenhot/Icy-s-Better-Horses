package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public class AppaloosaHorse extends MediumHorse {

    private static final int NIGHT_VISION_TICKS = 300;
    private static final int NIGHT_VISION_REFRESH_TICKS = 100;

    public AppaloosaHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || this.tickCount % NIGHT_VISION_REFRESH_TICKS != 0) {
            return;
        }

        net.minecraft.world.entity.player.Player rider = BhBreedAbilities.rider(this);
        if (rider != null && BhBreedAbilities.isDarkOutside(this)) {
            BhBreedAbilities.applyQuietEffect(
                    rider, net.minecraft.world.effect.MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0);
        }
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.APPALOOSA;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.APPALOOSA;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.245D)
                .add(Attributes.JUMP_STRENGTH, 0.75D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
