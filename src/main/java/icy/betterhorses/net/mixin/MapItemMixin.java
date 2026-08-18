package icy.betterhorses.net.mixin;

import icy.betterhorses.net.entity.AmericanPaintHorse;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @ModifyConstant(method = "update", constant = @Constant(intValue = 128, ordinal = 0))
    private int bh_widenTrailBlazerScan(int scanWidth, Level level, Entity entity, MapItemSavedData data) {
        return entity.getVehicle() instanceof AmericanPaintHorse ? scanWidth * 2 : scanWidth;
    }
}
