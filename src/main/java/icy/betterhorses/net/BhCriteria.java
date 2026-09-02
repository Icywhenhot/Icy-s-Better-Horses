package icy.betterhorses.net;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

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

    public static void init() {
        Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "milestone"),
                MILESTONE);
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

        @Override
        public Codec<TriggerInstance> codec() {
            return TriggerInstance.CODEC;
        }

        public void fire(ServerPlayer player, String key, int value) {
            this.trigger(player, instance -> instance.matches(key, value));
        }

        public record TriggerInstance(Optional<ContextAwarePredicate> player, String key, MinMaxBounds.Ints value)
                implements SimpleCriterionTrigger.SimpleInstance {

            public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                    Codec.STRING.fieldOf("key").forGetter(TriggerInstance::key),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("value", MinMaxBounds.Ints.ANY)
                            .forGetter(TriggerInstance::value)
            ).apply(instance, TriggerInstance::new));

            public boolean matches(String key, int value) {
                return this.key.equals(key) && this.value.matches(value);
            }
        }
    }

    private BhCriteria() {}
}
