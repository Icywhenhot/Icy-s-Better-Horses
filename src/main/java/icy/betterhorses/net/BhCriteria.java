package icy.betterhorses.net;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class BhCriteria {

    public static final String OWN_HORSE = "own_horse";
    public static final String HORSE_COUNT = "horse_count";
    public static final String BOND_MAX = "bond_max";
    public static final String SECOND_CHANCE = "second_chance";
    public static final String SET_HOME = "set_home";
    public static final String ENDER_CHEST_GEAR = "ender_chest_gear";
    public static final String STABILIZER_LANDING = "stabilizer_landing";
    public static final String TOP_SPEED = "top_speed";
    public static final String FOAL = "foal";
    public static final String MIXED_FOAL = "mixed_foal";
    public static final String BREED_PREFIX = "breed/";

    public static final Milestone MILESTONE = new Milestone();

    private BhCriteria() {}

    public static void register() {
        CriteriaTriggers.register(MILESTONE);
    }

    public static void fire(@Nullable ServerPlayer player, String key) {
        fire(player, key, 0);
    }

    public static void fire(@Nullable ServerPlayer player, String key, int value) {
        if (player == null) return;
        MILESTONE.fire(player, key, value);
    }

    public static void fireBreed(@Nullable ServerPlayer player, HorseBreed breed) {
        if (player == null || !breed.isRealBreed()) return;
        fire(player, BREED_PREFIX + breed.name().toLowerCase(Locale.ROOT));
    }

    public static void fireOwnedHorseCount(@Nullable ServerPlayer player) {
        if (player == null) return;
        fire(player, HORSE_COUNT, HorseTracker.findAllStoredHorsesOwnedBy(player.getUUID()).size());
    }

    public static final class Milestone extends SimpleCriterionTrigger<Milestone.TriggerInstance> {

        static final ResourceLocation ID =
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "milestone");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                                 DeserializationContext context) {
            return new TriggerInstance(player,
                    GsonHelper.getAsString(json, "key"),
                    MinMaxBounds.Ints.fromJson(json.get("value")));
        }

        public void fire(ServerPlayer player, String key, int value) {
            this.trigger(player, instance -> instance.matches(key, value));
        }

        public static final class TriggerInstance extends AbstractCriterionTriggerInstance {

            private final String key;
            private final MinMaxBounds.Ints value;

            TriggerInstance(ContextAwarePredicate player, String key, MinMaxBounds.Ints value) {
                super(ID, player);
                this.key = key;
                this.value = value;
            }

            @Override
            public JsonObject serializeToJson(SerializationContext context) {
                JsonObject json = super.serializeToJson(context);
                json.addProperty("key", this.key);
                json.add("value", this.value.serializeToJson());
                return json;
            }

            boolean matches(String key, int value) {
                return this.key.equals(key) && this.value.matches(value);
            }
        }
    }
}
