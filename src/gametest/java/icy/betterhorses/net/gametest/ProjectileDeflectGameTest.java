package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.block.Blocks;

public class ProjectileDeflectGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 60)
    public void piercingArrowIsDeflectedNotLooped(GameTestHelper helper) {
        helper.setBlock(4, 1, 4, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 4, 2, 4);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setBond(100);

        Arrow arrow = new Arrow(level, horse.getX() - 3.0, horse.getY() + horse.getBbHeight() / 2.0, horse.getZ());
        arrow.setPierceLevel((byte) 3);
        arrow.shoot(1.0, 0.0, 0.0, 1.0F, 0.0F);
        arrow.setNoGravity(true);
        level.addFreshEntity(arrow);

        helper.succeedWhen(() -> {
            helper.assertTrue(horse.isAlive(), "horse should still be alive");
            helper.assertTrue(arrow.getPierceLevel() == 0,
                    "deflecting the arrow should clear its pierce level so it stops re-hitting the same horse");
        });
    }
}
