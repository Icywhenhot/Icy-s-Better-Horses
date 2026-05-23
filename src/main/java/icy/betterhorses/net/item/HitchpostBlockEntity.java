package icy.betterhorses.net.item;

import icy.betterhorses.net.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class HitchpostBlockEntity extends BlockEntity {

    private @Nullable UUID tetheredHorseId;

    public HitchpostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HITCHPOST, pos, state);
    }

    public @Nullable UUID getTetheredHorseId() {
        return tetheredHorseId;
    }

    public void setTetheredHorseId(@Nullable UUID horseId) {
        if (Objects.equals(this.tetheredHorseId, horseId)) {
            return;
        }

        this.tetheredHorseId = horseId;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tetheredHorseId = tag.hasUUID("TetheredHorse") ? tag.getUUID("TetheredHorse") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.tetheredHorseId != null) {
            tag.putUUID("TetheredHorse", this.tetheredHorseId);
        }
    }
}
