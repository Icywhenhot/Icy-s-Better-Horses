package icy.betterhorses.net.item;

import icy.betterhorses.net.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * 1.21.5+ block-entity save format: {@code loadAdditional}/{@code saveAdditional} now take
 * {@link ValueInput}/{@link ValueOutput} (codec-based) instead of {@code CompoundTag}.
 * UUIDs are stored via {@link UUIDUtil#CODEC}.
 */
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tetheredHorseId = input.read("TetheredHorse", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.tetheredHorseId != null) {
            output.store("TetheredHorse", UUIDUtil.CODEC, this.tetheredHorseId);
        }
    }
}
