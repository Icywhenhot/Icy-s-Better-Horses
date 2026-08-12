package icy.betterhorses.net;

import icy.betterhorses.net.entity.AmericanPaintHorse;
import icy.betterhorses.net.entity.AndalusianHorse;
import icy.betterhorses.net.entity.AppaloosaHorse;
import icy.betterhorses.net.entity.FriesianHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.entity.MediumHorse;
import icy.betterhorses.net.entity.MustangHorse;
import icy.betterhorses.net.entity.QuarterHorse;
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
                    // tight tracking so the cart stays glued to the horse with minimal sync lag
                    .clientTrackingRange(11)
                    .updateInterval(1)
                    .build(key("horse_cart")));

    public static final EntityType<IcelandicHorse> ICELANDIC_HORSE = register(
            "icelandic_horse",
            EntityType.Builder.<IcelandicHorse>of(IcelandicHorse::new, MobCategory.CREATURE)
                    .sized(IcelandicHorse.WIDTH, IcelandicHorse.HEIGHT)
                    // without these the rider sits at the entity origin instead of on the
                    // saddle, and mobs aim at the wrong height. vanilla horse uses
                    // 1.52 / 1.44375 against a 1.6 tall body; scaled for a 1.45 pony
                    .eyeHeight(IcelandicHorse.HEIGHT * 0.95F)
                    .passengerAttachments(IcelandicHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("icelandic_horse")));

    public static final EntityType<FriesianHorse> FRIESIAN_HORSE = register(
            "friesian_horse",
            EntityType.Builder.<FriesianHorse>of(FriesianHorse::new, MobCategory.CREATURE)
                    .sized(FriesianHorse.WIDTH, FriesianHorse.HEIGHT)
                    // same ratios as the Icelandic against a taller body: 0.90 puts the
                    // rider just above the barrel, which on a Friesian is bb y=25
                    .eyeHeight(FriesianHorse.HEIGHT * 0.95F)
                    .passengerAttachments(FriesianHorse.HEIGHT * 0.90F)
                    .clientTrackingRange(10)
                    .build(key("friesian_horse")));

    // --- the medium size class -------------------------------------------------------
    // One mesh, three breeds. They share MediumHorse.WIDTH/HEIGHT because they share the
    // model; only their coats and their stat blocks differ.
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

    private static <T extends MediumHorse> EntityType<T> registerMedium(
            String path, EntityType.EntityFactory<T> factory) {
        return register(path, EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                // without these the rider sits at the entity origin instead of on the
                // saddle, and mobs aim at the wrong height
                .eyeHeight(MediumHorse.HEIGHT * 0.95F)
                .passengerAttachments(MediumHorse.HEIGHT * 0.90F)
                .clientTrackingRange(10)
                .build(key(path)));
    }

    public static void init() {
        // registration happens in the static field initializers; touching the class triggers it.
        // attributes must be registered separately or the entity fails to spawn
        FabricDefaultAttributeRegistry.register(ICELANDIC_HORSE, IcelandicHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(FRIESIAN_HORSE, FriesianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(APPALOOSA_HORSE, AppaloosaHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(MUSTANG_HORSE, MustangHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(QUARTER_HORSE, QuarterHorse.createAttributes());
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
