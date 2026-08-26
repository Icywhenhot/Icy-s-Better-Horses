package icy.betterhorses.net;

import icy.betterhorses.net.entity.AmericanPaintHorse;
import icy.betterhorses.net.entity.AndalusianHorse;
import icy.betterhorses.net.entity.AppaloosaHorse;
import icy.betterhorses.net.entity.FriesianHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.entity.MediumHorse;
import icy.betterhorses.net.entity.MustangHorse;
import icy.betterhorses.net.entity.PercheronHorse;
import icy.betterhorses.net.entity.QuarterHorse;
import icy.betterhorses.net.entity.BelgianHorse;
import icy.betterhorses.net.entity.ClydesdaleHorse;
import icy.betterhorses.net.entity.ShireHorse;
import icy.betterhorses.net.entity.ThoroughbredHorse;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

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
        FabricDefaultAttributeRegistry.register(ICELANDIC_HORSE, IcelandicHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(FRIESIAN_HORSE, FriesianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(APPALOOSA_HORSE, AppaloosaHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(MUSTANG_HORSE, MustangHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(QUARTER_HORSE, QuarterHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(PERCHERON_HORSE, PercheronHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(SHIRE_HORSE, ShireHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(BELGIAN_HORSE, BelgianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(CLYDESDALE_HORSE, ClydesdaleHorse.createAttributes());
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String path, EntityType<T> type) {
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                type);
    }

    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }

    private ModEntities() {}
}
