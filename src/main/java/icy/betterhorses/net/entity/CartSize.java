package icy.betterhorses.net.entity;

import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.resources.Identifier;

import com.geckolib.animation.RawAnimation;

public enum CartSize {

    NORMAL("horse_cart", "chest",
            "wheel moving2", "chest", "chest close", "stand alone",
            2.2D, 1.15D, 2.55D, 0.0D, 2, 0, 54),

    LARGE("horse_cart_large", "chest2",
            "wheel moving", "chestopen", "chest close", "idle",
            2.9D, 1.8D, 3.3D, 0.65D, 4, 2, CartChestMenu.SLOTS);

    private final Identifier model;
    private final Identifier texture;
    private final Identifier animation;
    private final String chestBone;

    private final RawAnimation wheelsRolling;
    private final RawAnimation chestOpening;
    private final RawAnimation chestClosing;
    private final RawAnimation standing;

    private final double bedCenterBehind;
    private final double bedHalfLength;
    private final double rearSeatBehind;
    private final double rearRowSpacing;
    private final int rearSeatCount;
    private final int rearSeatsWithChest;
    private final int chestSlots;

    CartSize(String asset, String chestBone,
             String wheelAnim, String chestOpenAnim, String chestCloseAnim, String standAnim,
             double bedCenterBehind, double bedHalfLength, double rearSeatBehind,
             double rearRowSpacing, int rearSeatCount, int rearSeatsWithChest, int chestSlots) {
        this.model = id(asset);
        this.texture = id("textures/entity/" + asset + ".png");
        this.animation = id(asset);
        this.chestBone = chestBone;
        this.wheelsRolling = RawAnimation.begin().thenLoop(wheelAnim);
        this.chestOpening = RawAnimation.begin().thenPlayAndHold(chestOpenAnim);
        this.chestClosing = RawAnimation.begin().thenPlayAndHold(chestCloseAnim);
        this.standing = RawAnimation.begin().thenLoop(standAnim);
        this.bedCenterBehind = bedCenterBehind;
        this.bedHalfLength = bedHalfLength;
        this.rearSeatBehind = rearSeatBehind;
        this.rearRowSpacing = rearRowSpacing;
        this.rearSeatCount = rearSeatCount;
        this.rearSeatsWithChest = rearSeatsWithChest;
        this.chestSlots = chestSlots;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
    }

    public static CartSize forArchetype(BreedArchetype archetype) {
        return archetype == BreedArchetype.DRAFT ? LARGE : NORMAL;
    }

    public static CartSize byLarge(boolean large) {
        return large ? LARGE : NORMAL;
    }

    public boolean isLarge() {
        return this == LARGE;
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

    public double bedCenterBehind() {
        return this.bedCenterBehind;
    }

    public double bedHalfLength() {
        return this.bedHalfLength;
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

    public int chestSlots() {
        return this.chestSlots;
    }

    public int rearSeats(boolean withChest) {
        return withChest ? this.rearSeatsWithChest : this.rearSeatCount;
    }
}
