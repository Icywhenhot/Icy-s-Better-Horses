package icy.betterhorses.net;

import icy.betterhorses.net.entity.AmericanPaintHorse;
import icy.betterhorses.net.entity.AndalusianHorse;
import icy.betterhorses.net.entity.AppaloosaHorse;
import icy.betterhorses.net.entity.FriesianHorse;
import icy.betterhorses.net.entity.HaflingerHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.entity.ArabianHorse;
import icy.betterhorses.net.entity.MediumHorse;
import icy.betterhorses.net.entity.MorganHorse;
import icy.betterhorses.net.entity.SmallHorse;
import icy.betterhorses.net.entity.MustangHorse;
import icy.betterhorses.net.entity.PercheronHorse;
import icy.betterhorses.net.entity.QuarterHorse;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.entity.BelgianHorse;
import icy.betterhorses.net.entity.ClydesdaleHorse;
import icy.betterhorses.net.entity.ShireHorse;
import icy.betterhorses.net.entity.ThoroughbredHorse;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModEntities {

    private static final Map<String, EntityType<?>> TYPES = new LinkedHashMap<>();

    public static final EntityType<HorseCartEntity> HORSE_CART = register(
            "horse_cart",
            EntityType.Builder.of(HorseCartEntity::new, MobCategory.MISC)
                    .sized(HorseCartEntity.WIDTH, HorseCartEntity.HEIGHT)
                    .clientTrackingRange(11)
                    .updateInterval(1)
                    .build(key("horse_cart")));

    public static final EntityType<IcelandicHorse> ICELANDIC_HORSE = register(
            "icelandic_horse",
            EntityType.Builder.<IcelandicHorse>of(IcelandicHorse::new, MobCategory.CREATURE)
                    .sized(IcelandicHorse.WIDTH, IcelandicHorse.HEIGHT)
                    .eyeHeight(IcelandicHorse.HEIGHT * 0.95F)
                    .passengerAttachments(IcelandicHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("icelandic_horse")));

    public static final EntityType<FriesianHorse> FRIESIAN_HORSE = register(
            "friesian_horse",
            EntityType.Builder.<FriesianHorse>of(FriesianHorse::new, MobCategory.CREATURE)
                    .sized(FriesianHorse.WIDTH, FriesianHorse.HEIGHT)
                    .eyeHeight(FriesianHorse.HEIGHT * 0.95F)
                    .passengerAttachments(FriesianHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("friesian_horse")));

    public static final EntityType<HaflingerHorse> HAFLINGER_HORSE = register(
            "haflinger_horse",
            EntityType.Builder.<HaflingerHorse>of(HaflingerHorse::new, MobCategory.CREATURE)
                    .sized(HaflingerHorse.WIDTH, HaflingerHorse.HEIGHT)
                    .eyeHeight(HaflingerHorse.HEIGHT * 0.95F)
                    .passengerAttachments(HaflingerHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("haflinger_horse")));

    public static final EntityType<AppaloosaHorse> APPALOOSA_HORSE =
            registerMedium("appaloosa_horse", AppaloosaHorse::new);
    public static final EntityType<ThoroughbredHorse> THOROUGHBRED_HORSE =
            registerMedium("thoroughbred_horse", ThoroughbredHorse::new);
    public static final EntityType<AmericanPaintHorse> AMERICAN_PAINT_HORSE =
            registerMedium("american_paint_horse", AmericanPaintHorse::new);
    public static final EntityType<AndalusianHorse> ANDALUSIAN_HORSE =
            registerMedium("andalusian_horse", AndalusianHorse::new);
    public static final EntityType<MustangHorse> MUSTANG_HORSE =
            registerMedium("mustang_horse", MustangHorse::new);
    public static final EntityType<QuarterHorse> QUARTER_HORSE =
            registerMedium("quarter_horse", QuarterHorse::new);

    public static final EntityType<ArabianHorse> ARABIAN_HORSE =
            registerSmall("arabian_horse", ArabianHorse::new);
    public static final EntityType<MorganHorse> MORGAN_HORSE =
            registerSmall("morgan_horse", MorganHorse::new);

    public static final EntityType<PercheronHorse> PERCHERON_HORSE =
            register("percheron_horse", EntityType.Builder.of(PercheronHorse::new, MobCategory.CREATURE)
                    .sized(PercheronHorse.WIDTH, PercheronHorse.HEIGHT)
                    .eyeHeight(PercheronHorse.HEIGHT * 0.95F)
                    .passengerAttachments(PercheronHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("percheron_horse")));

    public static final EntityType<ShireHorse> SHIRE_HORSE =
            register("shire_horse", EntityType.Builder.of(ShireHorse::new, MobCategory.CREATURE)
                    .sized(ShireHorse.WIDTH, ShireHorse.HEIGHT)
                    .eyeHeight(ShireHorse.HEIGHT * 0.95F)
                    .passengerAttachments(ShireHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("shire_horse")));

    public static final EntityType<BelgianHorse> BELGIAN_HORSE =
            register("belgian_horse", EntityType.Builder.of(BelgianHorse::new, MobCategory.CREATURE)
                    .sized(BelgianHorse.WIDTH, BelgianHorse.HEIGHT)
                    .eyeHeight(BelgianHorse.HEIGHT * 0.95F)
                    .passengerAttachments(BelgianHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("belgian_horse")));

    public static final EntityType<ClydesdaleHorse> CLYDESDALE_HORSE =
            register("clydesdale_horse", EntityType.Builder.of(ClydesdaleHorse::new, MobCategory.CREATURE)
                    .sized(ClydesdaleHorse.WIDTH, ClydesdaleHorse.HEIGHT)
                    .eyeHeight(ClydesdaleHorse.HEIGHT * 0.95F)
                    .passengerAttachments(ClydesdaleHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("clydesdale_horse")));

    public static @Nullable EntityType<? extends BhBreedHorse> forBreed(ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().get(breedKey.location());
        if (type == null) return null;
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(type.entityType().location()).orElse(null);
        if (entityType == null) return null;
        @SuppressWarnings("unchecked")
        EntityType<? extends BhBreedHorse> result = (EntityType<? extends BhBreedHorse>) entityType;
        return result;
    }

    /** Compatibility overload retained for staged client/common callers. */
    public static EntityType<? extends BhBreedHorse> forBreed(HorseBreed breed) {
        ResourceKey<BreedType> key = BhBreeds.keyOf(breed);
        EntityType<? extends BhBreedHorse> result = key == null ? null : forBreed(key);
        return result != null ? result : MUSTANG_HORSE;
    }

    private static <T extends SmallHorse> EntityType<T> registerSmall(
            String path, EntityType.EntityFactory<T> factory) {
        return register(path, EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(SmallHorse.WIDTH, SmallHorse.HEIGHT)
                .eyeHeight(SmallHorse.HEIGHT * 0.95F)
                .passengerAttachments(SmallHorse.HEIGHT * 0.90F)
                .clientTrackingRange(10)
                .build(key(path)));
    }

    private static <T extends MediumHorse> EntityType<T> registerMedium(
            String path, EntityType.EntityFactory<T> factory) {
        return register(path, EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                .eyeHeight(MediumHorse.HEIGHT * 0.95F)
                .passengerAttachments(MediumHorse.HEIGHT * 0.90F)
                .clientTrackingRange(10)
                .build(key(path)));
    }

    public static void init() {
        TYPES.forEach((path, value) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path), value));
        registerAttributes();
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(ICELANDIC_HORSE, IcelandicHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(FRIESIAN_HORSE, FriesianHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(HAFLINGER_HORSE, HaflingerHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(APPALOOSA_HORSE, AppaloosaHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MUSTANG_HORSE, MustangHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(QUARTER_HORSE, QuarterHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(ARABIAN_HORSE, ArabianHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MORGAN_HORSE, MorganHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(PERCHERON_HORSE, PercheronHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SHIRE_HORSE, ShireHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BELGIAN_HORSE, BelgianHorse.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CLYDESDALE_HORSE, ClydesdaleHorse.createAttributes().build());
    }

    private static <T extends Entity> EntityType<T> register(
            String path, EntityType<T> type) {
        TYPES.put(path, type);
        return type;
    }

    private static String key(String path) {
        return ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path).toString();
    }

    private ModEntities() {}
}
