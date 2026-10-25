package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Bonding implements HorseFeature {

    private static final double RIDE_BLOCKS = 1000.0D;
    private static final double MAX_STEP = 4.0D;
    private static final int FULL_JUMP = 90;
    private static final double BIG_JUMP_RISE = 2.0D;
    private static final int JUMP_TIMEOUT = 60;
    private static final int JUMPS_PER_BOND = 10;
    private static final int JUMP_BOND_CAP = 2;
    private static final int KILL_CAP = 3;
    private static final int SAVE_CAP = 2;
    private static final int SAVE_BOND = 2;
    public static final int SAVE_WINDOW = 100;
    private static final int HIT_LOSS = 2;
    private static final int TREAT_COOLDOWN = 6000;
    private static final int FOAL_CAP = 50;
    private static final TagKey<Item> SNACKS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "horse_snacks"));
    private static final Item[] TREATS = {Items.WHEAT, Items.SUGAR, Items.APPLE, Items.GOLDEN_CARROT};

    private double ridden;
    private double lastx = Double.NaN;
    private double lasty;
    private double lastz;
    private long day = -1;
    private int jumps;
    private int jumpBond;
    private int kills;
    private int saves;
    private double jumpFrom = Double.NaN;
    private double jumpPeak;
    private int jumpTicks;
    private @Nullable ResourceLocation treat;
    private boolean treatKnown;
    private long treatReadyAt;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide()) {
            return;
        }
        double x = horse.getX();
        double y = horse.getY();
        double z = horse.getZ();
        double step = Double.isNaN(lastx) ? 0.0D
                : Math.sqrt((x - lastx) * (x - lastx) + (y - lasty) * (y - lasty) + (z - lastz) * (z - lastz));
        lastx = x;
        lasty = y;
        lastz = z;

        UUID owner = data.bh_getOwner();
        if (owner != null && step <= MAX_STEP && !horse.isInWater() && !horse.isPassenger()
                && horse.getPassengers().stream().anyMatch(p -> owner.equals(p.getUUID()))) {
            ridden += step;
            if (ridden >= RIDE_BLOCKS) {
                ridden -= RIDE_BLOCKS;
                BhHorseTraits.grantBond(data, 1);
            }
        }

        if (!(horse.getControllingPassenger() instanceof ServerPlayer rider)
                || !rider.getUUID().equals(data.bh_getOwner())) {
            jumpFrom = Double.NaN;
            return;
        }

        if (!Double.isNaN(jumpFrom)) {
            jumpPeak = Math.max(jumpPeak, horse.getY());
            jumpTicks++;
            if (jumpTicks > 3 && horse.onGround()) {
                if (jumpPeak - jumpFrom >= BIG_JUMP_RISE) {
                    bigJump(horse, data);
                }
                jumpFrom = Double.NaN;
            } else if (jumpTicks > JUMP_TIMEOUT) {
                jumpFrom = Double.NaN;
            }
        }
    }

    public void startJump(AbstractHorse horse, int power) {
        if (power < FULL_JUMP) {
            return;
        }
        jumpFrom = horse.getY();
        jumpPeak = jumpFrom;
        jumpTicks = 0;
    }

    private void bigJump(AbstractHorse horse, IHorseData data) {
        rollDay(horse);
        if (jumpBond >= JUMP_BOND_CAP) {
            return;
        }
        jumps++;
        if (jumps >= JUMPS_PER_BOND) {
            jumps = 0;
            jumpBond++;
            BhHorseTraits.grantBond(data, 1);
        }
    }

    public void mountedKill(AbstractHorse horse, IHorseData data) {
        rollDay(horse);
        if (kills < KILL_CAP) {
            kills++;
            BhHorseTraits.grantBond(data, 1);
        }
    }

    public void saved(AbstractHorse horse, IHorseData data) {
        rollDay(horse);
        if (saves < SAVE_CAP) {
            saves++;
            BhHorseTraits.grantBond(data, SAVE_BOND);
        }
    }

    public static void hitByOwner(IHorseData data) {
        data.bh_setBond(data.bh_getBond() - HIT_LOSS);
    }

    public static int foalBond(IHorseData a, IHorseData b) {
        return Math.min(FOAL_CAP, (a.bh_getBond() + b.bh_getBond()) / 2);
    }

    private void rollDay(AbstractHorse horse) {
        long today = horse.level().getDayTime() / 24000L;
        if (today != day) {
            day = today;
            jumps = 0;
            jumpBond = 0;
            kills = 0;
            saves = 0;
        }
    }

    public boolean isTreat(AbstractHorse horse, ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(treat(horse));
    }

    public void fedTreat(AbstractHorse horse, IHorseData data) {
        if (!treatKnown) {
            treatKnown = true;
            data.bh_showTreat(treat.toString());
        }
        long now = horse.level().getGameTime();
        if (now >= treatReadyAt) {
            treatReadyAt = now + TREAT_COOLDOWN;
            BhHorseTraits.grantBond(data, 1);
        }
    }

    private ResourceLocation treat(AbstractHorse horse) {
        if (treat == null) {
            List<Item> pool = new ArrayList<>(List.of(TREATS));
            BuiltInRegistries.ITEM.getTag(SNACKS).ifPresent(set -> set.stream().map(Holder::value).forEach(pool::add));
            treat = BuiltInRegistries.ITEM.getKey(pool.get(horse.getRandom().nextInt(pool.size())));
        }
        return treat;
    }

    public void read(IHorseData data, CompoundTag tag) {
        ridden = tag.getDouble("Ridden");
        day = tag.contains("Day") ? tag.getLong("Day") : -1;
        jumps = tag.getInt("Jumps");
        jumpBond = tag.getInt("JumpBond");
        kills = tag.getInt("Kills");
        saves = tag.getInt("Saves");
        treat = tag.contains("Treat") ? ResourceLocation.tryParse(tag.getString("Treat")) : null;
        treatKnown = treat != null && tag.getBoolean("TreatKnown");
        treatReadyAt = tag.getLong("TreatReadyAt");
        data.bh_showTreat(treatKnown ? treat.toString() : "");
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Ridden", ridden);
        tag.putLong("Day", day);
        tag.putInt("Jumps", jumps);
        tag.putInt("JumpBond", jumpBond);
        tag.putInt("Kills", kills);
        tag.putInt("Saves", saves);
        if (treat != null) {
            tag.putString("Treat", treat.toString());
            tag.putBoolean("TreatKnown", treatKnown);
        }
        tag.putLong("TreatReadyAt", treatReadyAt);
        return tag;
    }
}
