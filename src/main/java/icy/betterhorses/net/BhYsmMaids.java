package icy.betterhorses.net;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;

final class BhYsmMaids {

    private BhYsmMaids() {}

    static boolean ysm(Entity entity) {
        return entity instanceof EntityMaid maid && maid.isYsmModel();
    }
}
