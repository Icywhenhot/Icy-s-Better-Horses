package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.player.Player;

public class ThoroughbredHorse extends MediumHorse {

    private static final int WIND_UP_TICKS = 3 * 20;
    private static final int BURST_TICKS = 5 * 20;
    private static final int COOLDOWN_TICKS = 10 * 20;
    private static final int WIND_UP_DECAY = 4;
    private static final int BURST_AMPLIFIER = 1;

    private final BhBreedAbilities.MovementSampler movement = new BhBreedAbilities.MovementSampler();
    private int flatRunTicks;
    private int burstTicks;
    private int cooldownTicks;

    public ThoroughbredHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        movement.sample(this);

        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (burstTicks > 0) {
            burstTicks--;
            if (burstTicks == 0) {
                cooldownTicks = COOLDOWN_TICKS;
            }
            return;
        }

        Player rider = BhBreedAbilities.rider(this);
        if (rider == null || !isGallopingGear() || !movement.isRunningFlat()) {
            flatRunTicks = Math.max(0, flatRunTicks - WIND_UP_DECAY);
            return;
        }

        flatRunTicks++;
        if (flatRunTicks >= WIND_UP_TICKS && cooldownTicks == 0) {
            flatRunTicks = 0;
            burstTicks = BURST_TICKS;
            BhBreedAbilities.grantSpeed(this, BURST_TICKS, BURST_AMPLIFIER);
            BhBreedAbilities.grantSpeed(rider, BURST_TICKS, BURST_AMPLIFIER);
        }
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.THOROUGHBRED;
    }

    private boolean isGallopingGear() {
        int gear = ((IHorseData) this).bh_getGear();
        return gear == 0 || gear == BhGears.TOP_GEAR;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.THOROUGHBRED;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3375D)
                .add(Attributes.JUMP_STRENGTH, 0.7D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
