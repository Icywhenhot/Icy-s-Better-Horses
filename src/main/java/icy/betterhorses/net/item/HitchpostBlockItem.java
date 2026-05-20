package icy.betterhorses.net.item;

import icy.betterhorses.net.ModBlocks;
import net.minecraft.world.item.BlockItem;

/**
 * Legacy wrapper retained so older references still compile. Hitchpost placement/tethering now
 * lives entirely on the block itself.
 */
@Deprecated(forRemoval = false)
public class HitchpostBlockItem extends BlockItem {

    public HitchpostBlockItem(Properties properties) {
        super(ModBlocks.HITCHPOST.get(), properties);
    }
}
