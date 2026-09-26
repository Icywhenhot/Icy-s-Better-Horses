package icy.betterhorses.net.gametest;

import com.mojang.authlib.GameProfile;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.UUID;

// Round 2, item 6: vanilla's actuallyHurt() returns early once armour/absorption fully soak a hit
// (verified with javap: an early RETURN once the mitigated damage hits zero, another for
// invulnerable). The rouse and medkit TAIL injects never used to run for those hits.
public class AbsorptionGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void ownerFullyAbsorbingAHitStillRousesTheirHorse(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        data.bh_setBond(60); // tier 1, needed for the defend-when-unridden branch

        ServerLevel level = helper.getLevel();
        // A real (unadded) ServerPlayer, not FakePlayer/the GameTest mock player - both of those
        // hard-code isInvulnerableTo() to always return true, which would defeat this test.
        ServerPlayer owner = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "bh-absorption-owner"));
        owner.setPos(helper.absoluteVec(new Vec3(2, 2, 3)));
        data.bh_setOwner(owner.getUUID());
        HorseTracker.register(horse);

        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 2, 4);
        owner.setAbsorptionAmount(20.0F); // fully soaks the hit below - net health damage is zero

        DamageSource source = level.damageSources().mobAttack(zombie);
        // Skip the outer hurt() wrapper (and its own difficulty scaling etc.) and call
        // actuallyHurt() directly - the exact method the bug/fix is about.
        try {
            Method actuallyHurt = net.minecraft.world.entity.player.Player.class
                    .getDeclaredMethod("actuallyHurt", DamageSource.class, float.class);
            actuallyHurt.setAccessible(true);
            actuallyHurt.invoke(owner, source, 2.0F);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        helper.assertTrue(zombie.getUUID().equals(data.bh_getCombatTarget()),
                "the horse should still rouse and target the attacker even though the hit was fully absorbed");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void horseFullyAbsorbingAHitStillUsesItsMedkit(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setOwner(UUID.randomUUID());
        horse.setTamed(true);
        data.bh_getGearContainer().setItem(GearSlot.MEDKIT.ordinal(), new ItemStack(ModItems.HORSE_MEDKIT));
        horse.setHealth(horse.getMaxHealth() * 0.4F); // already below the 50% trigger threshold

        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 2, 4);
        horse.setAbsorptionAmount(20.0F); // fully soaks the hit below - net health damage is zero

        ServerLevel level = helper.getLevel();
        DamageSource source = level.damageSources().mobAttack(zombie);
        horse.hurt(source, 2.0F);

        helper.assertTrue(
                data.bh_getGearContainer().getItem(GearSlot.MEDKIT.ordinal()).isEmpty(),
                "the medkit should be consumed even though the hit's health damage was fully absorbed");
        helper.assertTrue(horse.hasEffect(MobEffects.REGENERATION),
                "the medkit's regeneration effect should have been applied");
        helper.succeed();
    }
}
