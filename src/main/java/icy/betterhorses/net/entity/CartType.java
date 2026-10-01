package icy.betterhorses.net.entity;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

public enum CartType {

    BUGGY("buggy", false, "horse_cart", "chest", "cartbody",
            "wheel moving2", "chest", "chest close", "stand alone", "till land",
            2.2D, 1.15D, 1.15D, 2.55D, 0.0D, 2, 0, 9, 2.375D,
            List.of(Attachment.CHEST, Attachment.PLOUGH), List.of()),

    WAGON("wagon", true, "horse_cart_large", "chest", "cart body",
            "wheel moving", "chest open", "chest close", "idle", null,
            2.875D, 1.8125D, 1.525D, 3.4D, 0.75D, 4, 2, 15, 3.9375D,
            List.of(Attachment.CHEST), List.of(new Part("tarp", "tarp", true)));

    public static final int CHEST_COLUMNS = 15;
    public static final int CHEST_ROWS = 6;
    public static final int CHEST_SLOTS = CHEST_COLUMNS * CHEST_ROWS;
    public static final int BENCH_SEATS = 2;

    public enum Attachment {
        CHEST, PLOUGH
    }

    public record Part(String key, String bone, boolean shades) {
        public Component label() {
            return Component.translatable("cart.icys-better-horses.part." + key);
        }
    }

    private final String id;
    private final boolean large;
    private final Identifier model;
    private final Identifier texture;
    private final Identifier animation;
    private final String chestBone;
    private final String bedBone;

    private final RawAnimation wheelsRolling;
    private final RawAnimation chestOpening;
    private final RawAnimation chestClosing;
    private final RawAnimation standing;
    private final @Nullable RawAnimation tilling;

    private final double bedCenterBehind;
    private final double bedHalfLength;
    private final double benchHeight;
    private final double rearSeatBehind;
    private final double rearRowSpacing;
    private final int rearSeatCount;
    private final int rearSeatsWithChest;
    private final int chestColumns;
    private final double axleBehind;
    private final List<Attachment> attachments;
    private final List<Part> parts;

    CartType(String id, boolean large, String asset, String chestBone, String bedBone,
             String wheelAnim, String chestOpenAnim, String chestCloseAnim, String standAnim,
             @Nullable String tillAnim,
             double bedCenterBehind, double bedHalfLength, double benchHeight,
             double rearSeatBehind, double rearRowSpacing,
             int rearSeatCount, int rearSeatsWithChest, int chestColumns, double axleBehind,
             List<Attachment> attachments, List<Part> parts) {
        this.id = id;
        this.large = large;
        this.model = id(asset);
        this.texture = id("textures/entity/" + asset + ".png");
        this.animation = id(asset);
        this.chestBone = chestBone;
        this.bedBone = bedBone;
        this.wheelsRolling = RawAnimation.begin().thenLoop(wheelAnim);
        this.chestOpening = RawAnimation.begin().thenPlayAndHold(chestOpenAnim);
        this.chestClosing = RawAnimation.begin().thenPlayAndHold(chestCloseAnim);
        this.standing = RawAnimation.begin().thenLoop(standAnim);
        this.tilling = tillAnim == null ? null : RawAnimation.begin().thenLoop(tillAnim);
        this.bedCenterBehind = bedCenterBehind;
        this.bedHalfLength = bedHalfLength;
        this.benchHeight = benchHeight;
        this.rearSeatBehind = rearSeatBehind;
        this.rearRowSpacing = rearRowSpacing;
        this.rearSeatCount = rearSeatCount;
        this.rearSeatsWithChest = rearSeatsWithChest;
        this.chestColumns = chestColumns;
        this.axleBehind = axleBehind;
        this.attachments = attachments;
        this.parts = parts;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
    }

    public static @Nullable CartType byId(String id) {
        for (CartType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static CartType byOrdinal(int ordinal) {
        CartType[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : BUGGY;
    }

    public static CartType defaultFor(boolean large) {
        return large ? WAGON : BUGGY;
    }

    public static boolean itemsOutside(@Nullable Container contents, CartType type) {
        if (contents == null) {
            return false;
        }
        for (int slot = 0; slot < contents.getContainerSize(); slot++) {
            if (slot % CHEST_COLUMNS >= type.chestColumns && !contents.getItem(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void widenLegacyRows(Container contents) {
        List<ItemStack> old = new ArrayList<>();
        for (int slot = 0; slot < contents.getContainerSize(); slot++) {
            old.add(contents.removeItemNoUpdate(slot));
        }
        List<ItemStack> spill = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            ItemStack stack = old.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int dest = i < 54 ? (i / 9) * CHEST_COLUMNS + i % 9 : i;
            if (dest < contents.getContainerSize() && contents.getItem(dest).isEmpty()) {
                contents.setItem(dest, stack);
            } else {
                spill.add(stack);
            }
        }
        for (ItemStack stack : spill) {
            for (int slot = 0; slot < contents.getContainerSize(); slot++) {
                if (contents.getItem(slot).isEmpty()) {
                    contents.setItem(slot, stack);
                    break;
                }
            }
        }
    }

    public String id() {
        return this.id;
    }

    public MutableComponent displayName() {
        return Component.translatable("cart.icys-better-horses." + this.id);
    }

    public boolean isLarge() {
        return this.large;
    }

    public Identifier model() {
        return this.model;
    }

    public Identifier texture() {
        return this.texture;
    }

    public Identifier animation() {
        return this.animation;
    }

    public String chestBone() {
        return this.chestBone;
    }

    public String bedBone() {
        return this.bedBone;
    }

    public RawAnimation wheelsRolling() {
        return this.wheelsRolling;
    }

    public RawAnimation chestOpening() {
        return this.chestOpening;
    }

    public RawAnimation chestClosing() {
        return this.chestClosing;
    }

    public RawAnimation standing() {
        return this.standing;
    }

    public @Nullable RawAnimation tilling() {
        return this.tilling;
    }

    public boolean takesPlough() {
        return this.attachments.contains(Attachment.PLOUGH);
    }

    public List<Attachment> attachments() {
        return this.attachments;
    }

    public List<Part> parts() {
        return this.parts;
    }

    public double axleBehind() {
        return this.axleBehind;
    }

    public double bedCenterBehind() {
        return this.bedCenterBehind;
    }

    public double bedHalfLength() {
        return this.bedHalfLength;
    }

    public double benchHeight() {
        return this.benchHeight;
    }

    public double rearSeatBehind() {
        return this.rearSeatBehind;
    }

    public double rearRowSpacing() {
        return this.rearRowSpacing;
    }

    public int rearSeatCount() {
        return this.rearSeatCount;
    }

    public int chestColumns() {
        return this.chestColumns;
    }

    public int chestSlots() {
        return this.chestColumns * CHEST_ROWS;
    }

    public int rearSeats(boolean withChest) {
        return withChest ? this.rearSeatsWithChest : this.rearSeatCount;
    }

    public int seats() {
        return BENCH_SEATS + this.rearSeatCount;
    }
}
