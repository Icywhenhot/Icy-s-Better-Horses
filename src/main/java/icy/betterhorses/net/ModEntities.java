package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.MediumHorse;
import icy.betterhorses.net.entity.SmallHorse;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.entity.FriesianHorse;
import icy.betterhorses.net.entity.HaflingerHorse;
import icy.betterhorses.net.entity.PercheronHorse;
import icy.betterhorses.net.entity.ShireHorse;
import icy.betterhorses.net.entity.BelgianHorse;
import icy.betterhorses.net.entity.ClydesdaleHorse;
import icy.betterhorses.net.entity.AppaloosaHorse;
import icy.betterhorses.net.entity.ThoroughbredHorse;
import icy.betterhorses.net.entity.AmericanPaintHorse;
import icy.betterhorses.net.entity.AndalusianHorse;
import icy.betterhorses.net.entity.MustangHorse;
import icy.betterhorses.net.entity.QuarterHorse;
import icy.betterhorses.net.entity.ArabianHorse;
import icy.betterhorses.net.entity.MorganHorse;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.Nullable;

public final class ModEntities {

    public static final EntityType<HorseCartEntity> HORSE_CART = register(
            "horse_cart",
            EntityType.Builder.<HorseCartEntity>of(HorseCartEntity::new, MobCategory.MISC)
                    .sized(HorseCartEntity.WIDTH, HorseCartEntity.HEIGHT)
                    .clientTrackingRange(11)
                    .updateInterval(1));

    public static final EntityType<IcelandicHorse> ICELANDIC_HORSE = register(
            "icelandic_horse",
            EntityType.Builder.<IcelandicHorse>of(IcelandicHorse::new, MobCategory.CREATURE)
                    .sized(IcelandicHorse.WIDTH, IcelandicHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<FriesianHorse> FRIESIAN_HORSE = register(
            "friesian_horse",
            EntityType.Builder.<FriesianHorse>of(FriesianHorse::new, MobCategory.CREATURE)
                    .sized(FriesianHorse.WIDTH, FriesianHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<HaflingerHorse> HAFLINGER_HORSE = register(
            "haflinger_horse",
            EntityType.Builder.<HaflingerHorse>of(HaflingerHorse::new, MobCategory.CREATURE)
                    .sized(HaflingerHorse.WIDTH, HaflingerHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<PercheronHorse> PERCHERON_HORSE = register(
            "percheron_horse",
            EntityType.Builder.<PercheronHorse>of(PercheronHorse::new, MobCategory.CREATURE)
                    .sized(PercheronHorse.WIDTH, PercheronHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<ShireHorse> SHIRE_HORSE = register(
            "shire_horse",
            EntityType.Builder.<ShireHorse>of(ShireHorse::new, MobCategory.CREATURE)
                    .sized(ShireHorse.WIDTH, ShireHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<BelgianHorse> BELGIAN_HORSE = register(
            "belgian_horse",
            EntityType.Builder.<BelgianHorse>of(BelgianHorse::new, MobCategory.CREATURE)
                    .sized(BelgianHorse.WIDTH, BelgianHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<ClydesdaleHorse> CLYDESDALE_HORSE = register(
            "clydesdale_horse",
            EntityType.Builder.<ClydesdaleHorse>of(ClydesdaleHorse::new, MobCategory.CREATURE)
                    .sized(ClydesdaleHorse.WIDTH, ClydesdaleHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<AppaloosaHorse> APPALOOSA_HORSE = register(
            "appaloosa_horse",
            EntityType.Builder.<AppaloosaHorse>of(AppaloosaHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<ThoroughbredHorse> THOROUGHBRED_HORSE = register(
            "thoroughbred_horse",
            EntityType.Builder.<ThoroughbredHorse>of(ThoroughbredHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<AmericanPaintHorse> AMERICAN_PAINT_HORSE = register(
            "american_paint_horse",
            EntityType.Builder.<AmericanPaintHorse>of(AmericanPaintHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<AndalusianHorse> ANDALUSIAN_HORSE = register(
            "andalusian_horse",
            EntityType.Builder.<AndalusianHorse>of(AndalusianHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<MustangHorse> MUSTANG_HORSE = register(
            "mustang_horse",
            EntityType.Builder.<MustangHorse>of(MustangHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<QuarterHorse> QUARTER_HORSE = register(
            "quarter_horse",
            EntityType.Builder.<QuarterHorse>of(QuarterHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<ArabianHorse> ARABIAN_HORSE = register(
            "arabian_horse",
            EntityType.Builder.<ArabianHorse>of(ArabianHorse::new, MobCategory.CREATURE)
                    .sized(SmallHorse.WIDTH, SmallHorse.HEIGHT)
                    .clientTrackingRange(10));

    public static final EntityType<MorganHorse> MORGAN_HORSE = register(
            "morgan_horse",
            EntityType.Builder.<MorganHorse>of(MorganHorse::new, MobCategory.CREATURE)
                    .sized(SmallHorse.WIDTH, SmallHorse.HEIGHT)
                    .clientTrackingRange(10));

    //No more jankyness!!!!! YIPPYYPYPYPYPPPYPY
    public static @Nullable EntityType<? extends BhBreedHorse> forBreed(ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().get(breedKey.location());
        if (type == null) {
            return null;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(type.entityType().location());
        @SuppressWarnings("unchecked")
        EntityType<? extends BhBreedHorse> result = (EntityType<? extends BhBreedHorse>) entityType;
        return result;
    }

    public static void register() {}

    public static void registerAttributes() {
        registerBreed(ICELANDIC_HORSE, IcelandicHorse.createAttributes());
        registerBreed(FRIESIAN_HORSE, FriesianHorse.createAttributes());
        registerBreed(HAFLINGER_HORSE, HaflingerHorse.createAttributes());
        registerBreed(PERCHERON_HORSE, PercheronHorse.createAttributes());
        registerBreed(SHIRE_HORSE, ShireHorse.createAttributes());
        registerBreed(BELGIAN_HORSE, BelgianHorse.createAttributes());
        registerBreed(CLYDESDALE_HORSE, ClydesdaleHorse.createAttributes());
        registerBreed(APPALOOSA_HORSE, AppaloosaHorse.createAttributes());
        registerBreed(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes());
        registerBreed(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes());
        registerBreed(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes());
        registerBreed(MUSTANG_HORSE, MustangHorse.createAttributes());
        registerBreed(QUARTER_HORSE, QuarterHorse.createAttributes());
        registerBreed(ARABIAN_HORSE, ArabianHorse.createAttributes());
        registerBreed(MORGAN_HORSE, MorganHorse.createAttributes());
    }

    private static void registerBreed(
            EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder) {
        FabricDefaultAttributeRegistry.register(type, builder);
    }

    private static <T extends Entity> EntityType<T> register(
            String path, EntityType.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, builder.build(id.toString()));
    }

    private ModEntities() {}
}
