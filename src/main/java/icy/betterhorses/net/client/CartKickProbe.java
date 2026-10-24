package icy.betterhorses.net.client;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Consumer;

public final class CartKickProbe {

    static final Deque<Object[]> steps = new ArrayDeque<>();
    static int wait = 0;
    static boolean opened = false;
    static int horseId = -1;
    static volatile int cartId = -1;
    static double bx, by, bz;

    public static void install() {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) tick();
        });
    }

    static void log(String s) {
        System.out.println("KPROBE " + s);
    }

    static void then(int delay, Runnable r) {
        steps.add(new Object[]{delay, r});
    }

    static void server(Consumer<MinecraftServer> r) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> {
            try {
                r.accept(server);
            } catch (Throwable t) {
                log("server step failed " + t);
                t.printStackTrace(System.out);
            }
        });
    }

    static ServerPlayer sp(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    static HorseCartEntity serverCart(MinecraftServer s) {
        AbstractHorse h = (AbstractHorse) sp(s).serverLevel().getEntity(horseId);
        return IHorseData.of(h).bh_getCartEntity();
    }

    static HorseCartEntity clientCart() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.getEntity(cartId) instanceof HorseCartEntity cart ? cart : null;
    }

    static void state(String tag) {
        Minecraft mc = Minecraft.getInstance();
        log(tag + " client vehicle=" + mc.player.getVehicle() + " shift=" + mc.player.isShiftKeyDown());
        server(s -> {
            ServerPlayer p = sp(s);
            HorseCartEntity cart = serverCart(s);
            log(tag + " server vehicle=" + p.getVehicle() + " vehicleRiders=" + (p.getVehicle() == null ? "-" : p.getVehicle().getPassengers()) + " cart=" + cart + " cartRiders=" + cart.getPassengers()
                    + " shift=" + p.isShiftKeyDown() + " pose=" + p.getPose());
        });
    }

    static void clickCart(String tag) {
        then(10, () -> {
            Minecraft mc = Minecraft.getInstance();
            HorseCartEntity cart = clientCart();
            if (cart == null) {
                log(tag + " no client cart");
                return;
            }
            log(tag + " click result=" + mc.gameMode.interactAt(mc.player, cart, new net.minecraft.world.phys.EntityHitResult(cart, cart.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND)
                    + " dist=" + mc.player.distanceTo(cart));
        });
        then(10, () -> state(tag));
    }

    static ServerPlayer partner;

    static void two(String tag) {
        server(s -> {
            AbstractHorse hh = (AbstractHorse) sp(s).serverLevel().getEntity(horseId);
            log(tag + " horse=" + hh + " alive=" + (hh != null && hh.isAlive()) + " cartGear=" + (hh != null && IHorseData.of(hh).bh_hasCartGear()) + " carts=" + sp(s).serverLevel().getEntitiesOfClass(HorseCartEntity.class, sp(s).getBoundingBox().inflate(30)));
            HorseCartEntity cart = serverCart(s);
            AbstractHorse h = (AbstractHorse) sp(s).serverLevel().getEntity(horseId);
            log(tag + " partnerVehicle=" + partner.getVehicle() + " cartRiders=" + cart.getPassengers()
                    + " horseRiders=" + h.getPassengers() + " devVehicle=" + sp(s).getVehicle());
        });
    }

    static void partnerClick(String tag) {
        then(10, () -> server(s -> {
            HorseCartEntity cart = serverCart(s);
            partner.teleportTo(cart.getBoundingBox().getCenter().x, by, cart.getBoundingBox().getCenter().z + 1.5);
            log(tag + " partner click=" + cart.interactAt(partner, Vec3.ZERO, InteractionHand.MAIN_HAND));
        }));
        then(5, () -> two(tag));
    }

    static void plan() {
        then(40, () -> server(s -> {
            ServerPlayer sp = sp(s);
            ServerLevel level = sp.serverLevel();
            bx = Math.floor(sp.getX()) + 0.5;
            by = -60;
            bz = Math.floor(sp.getZ()) + 0.5;
            for (int x = -14; x <= 14; x++) for (int z = -14; z <= 14; z++) {
                level.setBlock(new BlockPos((int) bx + x, (int) by - 1, (int) bz + z), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            }
            AbstractHorse h = ModEntities.THOROUGHBRED_HORSE.get().create(level);
            h.moveTo(bx, by, bz, 90.0F, 0);
            h.setYBodyRot(90.0F);
            h.setTamed(true);
            level.addFreshEntity(h);
            IHorseData data = IHorseData.of(h);
            data.bh_setOwner(sp.getUUID());
            data.bh_equipUpgradedSaddle(new ItemStack(ModItems.UPGRADED_SADDLE.get()));
            data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART.get()));
            data.bh_getGearContainer().setItem(GearSlot.CHEST.ordinal(), new ItemStack(net.minecraft.world.item.Items.CHEST));
            horseId = h.getId();
            partner = net.minecraftforge.common.util.FakePlayerFactory.get(level,
                    new com.mojang.authlib.GameProfile(UUID.randomUUID(), "Partner"));
            partner.setPos(bx, by, bz + 3);
            log("chestGear=" + data.bh_hasGear(GearSlot.CHEST) + " exclusivity=" + BhConfig.horseExclusivityEnabled() + " multi=" + BhConfig.multiRidingEnabled());
        }));
        then(40, () -> two("start"));
        partnerClick("A-board-noDriver");
        then(10, () -> server(s -> {
            AbstractHorse h = (AbstractHorse) sp(s).serverLevel().getEntity(horseId);
            log("dev mounts " + sp(s).startRiding(h, true));
        }));
        then(10, () -> two("driverOn"));
        then(10, () -> server(s -> {
            serverCart(s).unloadPassengers();
            log("unloaded");
        }));
        then(5, () -> two("afterUnload"));
        then(80, () -> two("later"));
        partnerClick("B-reboard-driverOn");
        then(80, () -> server(s -> {
            partner.stopRiding();
            sp(s).stopRiding();
            log("both off");
        }));
        then(80, () -> two("off"));
        partnerClick("C-reboard-noDriver");
    }

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!opened) {
                if (mc.screen instanceof TitleScreen) {
                    opened = true;
                    wait = 100;
                    mc.options.pauseOnLostFocus = false;
                    plan();
                    then(10, () -> {
                        log("done");
                        mc.stop();
                    });
                    mc.createWorldOpenFlows().createFreshLevel("bhkick" + System.currentTimeMillis(),
                            new LevelSettings("bhkick" + System.currentTimeMillis(), GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                                    new GameRules(), WorldDataConfiguration.DEFAULT),
                            new WorldOptions(0L, false, false),
                            registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                                    .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
                }
                return;
            }
            if (mc.player == null || mc.getSingleplayerServer() == null) return;
            if (wait > 0) {
                wait--;
                return;
            }
            Object[] next = steps.poll();
            if (next == null) return;
            ((Runnable) next[1]).run();
            if (!steps.isEmpty()) wait = (Integer) steps.peek()[0];
        } catch (Throwable t) {
            log("failed " + t);
            t.printStackTrace(System.out);
            steps.clear();
            mc.stop();
        }
    }
}
