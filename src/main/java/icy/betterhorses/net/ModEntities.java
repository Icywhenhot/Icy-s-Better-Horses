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
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.entity.BhUndeadHorse;
import icy.betterhorses.net.entity.ClydesdaleHorse;
import icy.betterhorses.net.entity.ShireHorse;
import icy.betterhorses.net.entity.ThoroughbredHorse;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Holder;

import java.util.List;

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

    public static final EntityType<BhBreedDonkey> ANDALUSIAN_DONKEY =
            registerDonkey("andalusian_donkey", BhContent.ANDALUSIAN_DONKEY, 1.3964844F, 1.5F);
    public static final EntityType<BhBreedDonkey> ASINARA_DONKEY =
            registerDonkey("asinara_donkey", BhContent.ASINARA_DONKEY, 1.2F, 1.3125F);
    public static final EntityType<BhBreedDonkey> MAMMOTH_DONKEY =
            registerDonkey("mammoth_donkey", BhContent.MAMMOTH_DONKEY, 1.3964844F, 1.375F);
    public static final EntityType<BhBreedDonkey> MINIATURE_DONKEY =
            registerDonkey("miniature_donkey", BhContent.MINIATURE_DONKEY, 0.9F, 1.0625F);
    public static final EntityType<BhBreedDonkey> PEGA_DONKEY =
            registerDonkey("pega_donkey", BhContent.PEGA_DONKEY, 1.3964844F, 1.5625F);
    public static final EntityType<BhBreedDonkey> POITOU_DONKEY =
            registerDonkey("poitou_donkey", BhContent.POITOU_DONKEY, 1.3964844F, 1.5F);
    public static final EntityType<BhBreedDonkey> ZAMORANO_DONKEY =
            registerDonkey("zamorano_donkey", BhContent.ZAMORANO_DONKEY, 1.3964844F, 1.375F);

    public static final List<EntityType<BhBreedDonkey>> DONKEYS = List.of(ANDALUSIAN_DONKEY, ASINARA_DONKEY,
            MAMMOTH_DONKEY, MINIATURE_DONKEY, PEGA_DONKEY, POITOU_DONKEY, ZAMORANO_DONKEY);

    public static final EntityType<BhUndeadHorse> LARGE_SKELETON_HORSE =
            registerUndead("large_skeleton_horse", BhContent.LARGE_SKELETON_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_SKELETON_HORSE =
            registerUndead("medium_skeleton_horse", BhContent.MEDIUM_SKELETON_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_SKELETON_HORSE =
            registerUndead("small_skeleton_horse", BhContent.SMALL_SKELETON_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> LARGE_ZOMBIE_HORSE =
            registerUndead("large_zombie_horse", BhContent.LARGE_ZOMBIE_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_ZOMBIE_HORSE =
            registerUndead("medium_zombie_horse", BhContent.MEDIUM_ZOMBIE_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_ZOMBIE_HORSE =
            registerUndead("small_zombie_horse", BhContent.SMALL_ZOMBIE_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> LARGE_STRAY_HORSE =
            registerUndead("large_stray_horse", BhContent.LARGE_STRAY_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_STRAY_HORSE =
            registerUndead("medium_stray_horse", BhContent.MEDIUM_STRAY_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_STRAY_HORSE =
            registerUndead("small_stray_horse", BhContent.SMALL_STRAY_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> LARGE_SCULK_HORSE =
            registerUndead("large_sculk_horse", BhContent.LARGE_SCULK_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_SCULK_HORSE =
            registerUndead("medium_sculk_horse", BhContent.MEDIUM_SCULK_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_SCULK_HORSE =
            registerUndead("small_sculk_horse", BhContent.SMALL_SCULK_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> LARGE_HUSK_HORSE =
            registerUndead("large_husk_horse", BhContent.LARGE_HUSK_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_HUSK_HORSE =
            registerUndead("medium_husk_horse", BhContent.MEDIUM_HUSK_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_HUSK_HORSE =
            registerUndead("small_husk_horse", BhContent.SMALL_HUSK_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> LARGE_DROWNED_HORSE =
            registerUndead("large_drowned_horse", BhContent.LARGE_DROWNED_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> MEDIUM_DROWNED_HORSE =
            registerUndead("medium_drowned_horse", BhContent.MEDIUM_DROWNED_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final EntityType<BhUndeadHorse> SMALL_DROWNED_HORSE =
            registerUndead("small_drowned_horse", BhContent.SMALL_DROWNED_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);

    public static final List<EntityType<BhUndeadHorse>> SKELETONS =
            List.of(LARGE_SKELETON_HORSE, MEDIUM_SKELETON_HORSE, SMALL_SKELETON_HORSE);
    public static final List<EntityType<BhUndeadHorse>> ZOMBIES =
            List.of(LARGE_ZOMBIE_HORSE, MEDIUM_ZOMBIE_HORSE, SMALL_ZOMBIE_HORSE);
    public static final List<EntityType<BhUndeadHorse>> STRAYS =
            List.of(LARGE_STRAY_HORSE, MEDIUM_STRAY_HORSE, SMALL_STRAY_HORSE);
    public static final List<EntityType<BhUndeadHorse>> SCULKS =
            List.of(LARGE_SCULK_HORSE, MEDIUM_SCULK_HORSE, SMALL_SCULK_HORSE);
    public static final List<EntityType<BhUndeadHorse>> HUSKS =
            List.of(LARGE_HUSK_HORSE, MEDIUM_HUSK_HORSE, SMALL_HUSK_HORSE);
    public static final List<EntityType<BhUndeadHorse>> DROWNEDS =
            List.of(LARGE_DROWNED_HORSE, MEDIUM_DROWNED_HORSE, SMALL_DROWNED_HORSE);

    //No more jankyness!!!!! YIPPYYPYPYPYPPPYPY
    public static @Nullable EntityType<? extends BhBreedHorse> forBreed(ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().getValue(breedKey.identifier());
        if (type == null) {
            return null;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(type.entityType().identifier()).orElse(null);
        @SuppressWarnings("unchecked")
        EntityType<? extends BhBreedHorse> result = (EntityType<? extends BhBreedHorse>) entityType;
        return result;
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

    private static EntityType<BhUndeadHorse> registerUndead(String path, Holder.Reference<BreedType> breed,
                                                          boolean skeleton, float width, float height) {
        return register(path, EntityType.Builder.<BhUndeadHorse>of(
                        (type, level) -> new BhUndeadHorse(type, level, breed.key(), skeleton), MobCategory.CREATURE)
                .sized(width, height).eyeHeight(height * 0.95F).passengerAttachments(height * 0.90F)
                .clientTrackingRange(10).build(key(path)));
    }

    private static EntityType<BhBreedDonkey> registerDonkey(
            String path, Holder.Reference<BreedType> breed, float width, float height) {
        return register(path, EntityType.Builder.<BhBreedDonkey>of(
                        (type, level) -> new BhBreedDonkey(type, level, breed.key()), MobCategory.CREATURE)
                .sized(width, height)
                .eyeHeight(height * 0.95F)
                .passengerAttachments(height * 0.90F)
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
        FabricDefaultAttributeRegistry.register(ICELANDIC_HORSE, IcelandicHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(FRIESIAN_HORSE, FriesianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(HAFLINGER_HORSE, HaflingerHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(APPALOOSA_HORSE, AppaloosaHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(MUSTANG_HORSE, MustangHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(QUARTER_HORSE, QuarterHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(ARABIAN_HORSE, ArabianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(MORGAN_HORSE, MorganHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(PERCHERON_HORSE, PercheronHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(SHIRE_HORSE, ShireHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(BELGIAN_HORSE, BelgianHorse.createAttributes());
        FabricDefaultAttributeRegistry.register(CLYDESDALE_HORSE, ClydesdaleHorse.createAttributes());
        for (EntityType<BhBreedDonkey> donkey : DONKEYS) {
            FabricDefaultAttributeRegistry.register(donkey, BhBreedDonkey.createAttributes());
        }
        for (EntityType<BhUndeadHorse> horse : SKELETONS) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(true));
        for (EntityType<BhUndeadHorse> horse : ZOMBIES) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(false));
        for (EntityType<BhUndeadHorse> horse : STRAYS) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(true));
        for (EntityType<BhUndeadHorse> horse : SCULKS) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(true));
        for (EntityType<BhUndeadHorse> horse : HUSKS) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(false));
        for (EntityType<BhUndeadHorse> horse : DROWNEDS) FabricDefaultAttributeRegistry.register(horse, BhUndeadHorse.createAttributes(false));
    }

    private static <T extends Entity> EntityType<T> register(
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
