package icy.betterhorses.net.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Objects;

public final class HoofStepEvent extends Event implements ICancellableEvent {
    private final AbstractHorse horse;
    private final int hoof;
    private final Vec3 position;
    private final BlockState ground;
    private ParticleOptions particle;

    public HoofStepEvent(AbstractHorse horse, int hoof, Vec3 position, BlockState ground, ParticleOptions particle) {
        this.horse = horse;
        this.hoof = hoof;
        this.position = position;
        this.ground = ground;
        this.particle = particle;
    }

    public AbstractHorse horse() {
        return horse;
    }

    public int hoof() {
        return hoof;
    }

    public Vec3 position() {
        return position;
    }

    public BlockState ground() {
        return ground;
    }

    public ParticleOptions particle() {
        return particle;
    }

    public void setParticle(ParticleOptions particle) {
        this.particle = Objects.requireNonNull(particle);
    }
}
