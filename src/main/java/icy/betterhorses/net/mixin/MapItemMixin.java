package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @ModifyConstant(method = "update", constant = @Constant(intValue = 128, ordinal = 0))
    private int bh_widenTrailBlazerScan(int scanWidth, Level level, Entity entity, MapItemSavedData data) {
        if (!(entity.getVehicle() instanceof AbstractHorse horse) || !BhHorseKind.managed(horse)) {
            return scanWidth;
        }
        IHorseData d = IHorseData.of(horse);
        if (!Objects.equals(d.bh_getBreedKey(), BhContent.AMERICAN_PAINT.key()) || !BhAbility.PAINT_SCAN.on()) {
            return scanWidth;
        }
        return scanWidth * (2 + BhHorseTraits.bondTier(d.bh_getBond()));
    }

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void bh_markPassedStructures(ItemStack stack, Level world, Entity holder,
                                         int slot, boolean selected, CallbackInfo ci) {
        if (!(world instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % 20L != 0L || !(holder.getVehicle() instanceof AbstractHorse horse)
                || !BhHorseKind.managed(horse)) {
            return;
        }
        IHorseData d = IHorseData.of(horse);
        if (!Objects.equals(d.bh_getBreedKey(), BhContent.AMERICAN_PAINT.key())
                || !BhAbility.PAINT_STRUCTURES.on()
                || BhHorseTraits.bondTier(d.bh_getBond()) < 2) {
            return;
        }

        boolean marked = false;
        BlockPos at = horse.blockPosition();
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Structure s : level.structureManager().getAllStructuresAt(at).keySet()) {
            ResourceLocation id = reg.getKey(s);
            StructureStart start = level.structureManager().getStructureAt(at, s);
            if (id == null || !start.isValid()) {
                continue;
            }
            BlockPos mid = start.getBoundingBox().getCenter();
            MapItemSavedData.addTargetDecoration(stack,
                    new BlockPos(mid.getX(), at.getY(), mid.getZ()),
                    id + "@" + mid.getX() + "," + mid.getZ(),
                    MapDecoration.Type.RED_X);
            marked = true;
        }
        if (marked) {
            BhSurge.pulse(d, 0, 0);
        }
    }
}
