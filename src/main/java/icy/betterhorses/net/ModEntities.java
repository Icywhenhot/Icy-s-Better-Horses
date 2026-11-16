package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.entity.BhBreedMule;
import icy.betterhorses.net.entity.BhUndeadHorse;
import icy.betterhorses.net.registry.BhContent;
import java.util.List;
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
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    private static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<EntityType<HorseCartEntity>> HORSE_CART = TYPES.register(
            "horse_cart",
            () -> EntityType.Builder.<HorseCartEntity>of(HorseCartEntity::new, MobCategory.MISC)
                    .sized(HorseCartEntity.WIDTH, HorseCartEntity.HEIGHT)
                    .clientTrackingRange(11)
                    .updateInterval(1)
                    .build(key("horse_cart")));

    public static final RegistryObject<EntityType<IcelandicHorse>> ICELANDIC_HORSE = TYPES.register(
            "icelandic_horse",
            () -> EntityType.Builder.<IcelandicHorse>of(IcelandicHorse::new, MobCategory.CREATURE)
                    .sized(IcelandicHorse.WIDTH, IcelandicHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("icelandic_horse")));

    public static final RegistryObject<EntityType<FriesianHorse>> FRIESIAN_HORSE = TYPES.register(
            "friesian_horse",
            () -> EntityType.Builder.<FriesianHorse>of(FriesianHorse::new, MobCategory.CREATURE)
                    .sized(FriesianHorse.WIDTH, FriesianHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("friesian_horse")));

    public static final RegistryObject<EntityType<HaflingerHorse>> HAFLINGER_HORSE = TYPES.register(
            "haflinger_horse",
            () -> EntityType.Builder.<HaflingerHorse>of(HaflingerHorse::new, MobCategory.CREATURE)
                    .sized(HaflingerHorse.WIDTH, HaflingerHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("haflinger_horse")));

    public static final RegistryObject<EntityType<PercheronHorse>> PERCHERON_HORSE = TYPES.register(
            "percheron_horse",
            () -> EntityType.Builder.<PercheronHorse>of(PercheronHorse::new, MobCategory.CREATURE)
                    .sized(PercheronHorse.WIDTH, PercheronHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("percheron_horse")));

    public static final RegistryObject<EntityType<ShireHorse>> SHIRE_HORSE = TYPES.register(
            "shire_horse",
            () -> EntityType.Builder.<ShireHorse>of(ShireHorse::new, MobCategory.CREATURE)
                    .sized(ShireHorse.WIDTH, ShireHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("shire_horse")));

    public static final RegistryObject<EntityType<BelgianHorse>> BELGIAN_HORSE = TYPES.register(
            "belgian_horse",
            () -> EntityType.Builder.<BelgianHorse>of(BelgianHorse::new, MobCategory.CREATURE)
                    .sized(BelgianHorse.WIDTH, BelgianHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("belgian_horse")));

    public static final RegistryObject<EntityType<ClydesdaleHorse>> CLYDESDALE_HORSE = TYPES.register(
            "clydesdale_horse",
            () -> EntityType.Builder.<ClydesdaleHorse>of(ClydesdaleHorse::new, MobCategory.CREATURE)
                    .sized(ClydesdaleHorse.WIDTH, ClydesdaleHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("clydesdale_horse")));

    public static final RegistryObject<EntityType<AppaloosaHorse>> APPALOOSA_HORSE = TYPES.register(
            "appaloosa_horse",
            () -> EntityType.Builder.<AppaloosaHorse>of(AppaloosaHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("appaloosa_horse")));

    public static final RegistryObject<EntityType<ThoroughbredHorse>> THOROUGHBRED_HORSE = TYPES.register(
            "thoroughbred_horse",
            () -> EntityType.Builder.<ThoroughbredHorse>of(ThoroughbredHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("thoroughbred_horse")));

    public static final RegistryObject<EntityType<AmericanPaintHorse>> AMERICAN_PAINT_HORSE = TYPES.register(
            "american_paint_horse",
            () -> EntityType.Builder.<AmericanPaintHorse>of(AmericanPaintHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("american_paint_horse")));

    public static final RegistryObject<EntityType<AndalusianHorse>> ANDALUSIAN_HORSE = TYPES.register(
            "andalusian_horse",
            () -> EntityType.Builder.<AndalusianHorse>of(AndalusianHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("andalusian_horse")));

    public static final RegistryObject<EntityType<MustangHorse>> MUSTANG_HORSE = TYPES.register(
            "mustang_horse",
            () -> EntityType.Builder.<MustangHorse>of(MustangHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("mustang_horse")));

    public static final RegistryObject<EntityType<QuarterHorse>> QUARTER_HORSE = TYPES.register(
            "quarter_horse",
            () -> EntityType.Builder.<QuarterHorse>of(QuarterHorse::new, MobCategory.CREATURE)
                    .sized(MediumHorse.WIDTH, MediumHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("quarter_horse")));

    public static final RegistryObject<EntityType<ArabianHorse>> ARABIAN_HORSE = TYPES.register(
            "arabian_horse",
            () -> EntityType.Builder.<ArabianHorse>of(ArabianHorse::new, MobCategory.CREATURE)
                    .sized(SmallHorse.WIDTH, SmallHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("arabian_horse")));

    public static final RegistryObject<EntityType<MorganHorse>> MORGAN_HORSE = TYPES.register(
            "morgan_horse",
            () -> EntityType.Builder.<MorganHorse>of(MorganHorse::new, MobCategory.CREATURE)
                    .sized(SmallHorse.WIDTH, SmallHorse.HEIGHT)
                    .clientTrackingRange(10)
                    .build(key("morgan_horse")));

    public static final RegistryObject<EntityType<BhBreedDonkey>> ANDALUSIAN_DONKEY =
            registerDonkey("andalusian_donkey", BhContent.ANDALUSIAN_DONKEY, 1.3964844F, 1.5F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> ASINARA_DONKEY =
            registerDonkey("asinara_donkey", BhContent.ASINARA_DONKEY, 1.2F, 1.3125F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> MAMMOTH_DONKEY =
            registerDonkey("mammoth_donkey", BhContent.MAMMOTH_DONKEY, 1.3964844F, 1.6875F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> MINIATURE_DONKEY =
            registerDonkey("miniature_donkey", BhContent.MINIATURE_DONKEY, 0.9F, 1.0625F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> PEGA_DONKEY =
            registerDonkey("pega_donkey", BhContent.PEGA_DONKEY, 1.3964844F, 1.625F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> POITOU_DONKEY =
            registerDonkey("poitou_donkey", BhContent.POITOU_DONKEY, 1.3964844F, 1.6875F);
    public static final RegistryObject<EntityType<BhBreedDonkey>> ZAMORANO_DONKEY =
            registerDonkey("zamorano_donkey", BhContent.ZAMORANO_DONKEY, 1.3964844F, 1.375F);

    public static final List<RegistryObject<EntityType<BhBreedDonkey>>> DONKEYS = List.of(ANDALUSIAN_DONKEY, ASINARA_DONKEY,
            MAMMOTH_DONKEY, MINIATURE_DONKEY, PEGA_DONKEY, POITOU_DONKEY, ZAMORANO_DONKEY);

    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_SKELETON_HORSE =
            registerUndead("large_skeleton_horse", BhContent.LARGE_SKELETON_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_SKELETON_HORSE =
            registerUndead("medium_skeleton_horse", BhContent.MEDIUM_SKELETON_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_SKELETON_HORSE =
            registerUndead("small_skeleton_horse", BhContent.SMALL_SKELETON_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_ZOMBIE_HORSE =
            registerUndead("large_zombie_horse", BhContent.LARGE_ZOMBIE_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_ZOMBIE_HORSE =
            registerUndead("medium_zombie_horse", BhContent.MEDIUM_ZOMBIE_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_ZOMBIE_HORSE =
            registerUndead("small_zombie_horse", BhContent.SMALL_ZOMBIE_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_STRAY_HORSE =
            registerUndead("large_stray_horse", BhContent.LARGE_STRAY_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_STRAY_HORSE =
            registerUndead("medium_stray_horse", BhContent.MEDIUM_STRAY_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_STRAY_HORSE =
            registerUndead("small_stray_horse", BhContent.SMALL_STRAY_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_SCULK_HORSE =
            registerUndead("large_sculk_horse", BhContent.LARGE_SCULK_HORSE, true, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_SCULK_HORSE =
            registerUndead("medium_sculk_horse", BhContent.MEDIUM_SCULK_HORSE, true, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_SCULK_HORSE =
            registerUndead("small_sculk_horse", BhContent.SMALL_SCULK_HORSE, true, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_HUSK_HORSE =
            registerUndead("large_husk_horse", BhContent.LARGE_HUSK_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_HUSK_HORSE =
            registerUndead("medium_husk_horse", BhContent.MEDIUM_HUSK_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_HUSK_HORSE =
            registerUndead("small_husk_horse", BhContent.SMALL_HUSK_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> LARGE_DROWNED_HORSE =
            registerUndead("large_drowned_horse", BhContent.LARGE_DROWNED_HORSE, false, PercheronHorse.WIDTH, PercheronHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> MEDIUM_DROWNED_HORSE =
            registerUndead("medium_drowned_horse", BhContent.MEDIUM_DROWNED_HORSE, false, MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhUndeadHorse>> SMALL_DROWNED_HORSE =
            registerUndead("small_drowned_horse", BhContent.SMALL_DROWNED_HORSE, false, SmallHorse.WIDTH, SmallHorse.HEIGHT);

    public static final RegistryObject<EntityType<BhBreedMule>> SMALL_MULE = registerMule("small_mule", BhContent.SMALL_MULE,
            SmallHorse.WIDTH, SmallHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhBreedMule>> MEDIUM_MULE = registerMule("medium_mule", BhContent.MEDIUM_MULE,
            MediumHorse.WIDTH, MediumHorse.HEIGHT);
    public static final RegistryObject<EntityType<BhBreedMule>> LARGE_MULE = registerMule("large_mule", BhContent.LARGE_MULE,
            PercheronHorse.WIDTH, PercheronHorse.HEIGHT);

    public static final List<RegistryObject<EntityType<BhBreedMule>>> MULES = List.of(SMALL_MULE, MEDIUM_MULE, LARGE_MULE);

    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> SKELETONS =
            List.of(LARGE_SKELETON_HORSE, MEDIUM_SKELETON_HORSE, SMALL_SKELETON_HORSE);
    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> ZOMBIES =
            List.of(LARGE_ZOMBIE_HORSE, MEDIUM_ZOMBIE_HORSE, SMALL_ZOMBIE_HORSE);
    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> STRAYS =
            List.of(LARGE_STRAY_HORSE, MEDIUM_STRAY_HORSE, SMALL_STRAY_HORSE);
    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> SCULKS =
            List.of(LARGE_SCULK_HORSE, MEDIUM_SCULK_HORSE, SMALL_SCULK_HORSE);
    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> HUSKS =
            List.of(LARGE_HUSK_HORSE, MEDIUM_HUSK_HORSE, SMALL_HUSK_HORSE);
    public static final List<RegistryObject<EntityType<BhUndeadHorse>>> DROWNEDS =
            List.of(LARGE_DROWNED_HORSE, MEDIUM_DROWNED_HORSE, SMALL_DROWNED_HORSE);

    public static @Nullable EntityType<? extends BhBreedHorse> forBreed(ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().getValue(breedKey.location());
        if (type == null) {
            return null;
        }
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(type.entityType().location());
        @SuppressWarnings("unchecked")
        EntityType<? extends BhBreedHorse> result = (EntityType<? extends BhBreedHorse>) entityType;
        return result;
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ICELANDIC_HORSE.get(), IcelandicHorse.createAttributes().build());
        event.put(FRIESIAN_HORSE.get(), FriesianHorse.createAttributes().build());
        event.put(HAFLINGER_HORSE.get(), HaflingerHorse.createAttributes().build());
        event.put(PERCHERON_HORSE.get(), PercheronHorse.createAttributes().build());
        event.put(SHIRE_HORSE.get(), ShireHorse.createAttributes().build());
        event.put(BELGIAN_HORSE.get(), BelgianHorse.createAttributes().build());
        event.put(CLYDESDALE_HORSE.get(), ClydesdaleHorse.createAttributes().build());
        event.put(APPALOOSA_HORSE.get(), AppaloosaHorse.createAttributes().build());
        event.put(THOROUGHBRED_HORSE.get(), ThoroughbredHorse.createAttributes().build());
        event.put(AMERICAN_PAINT_HORSE.get(), AmericanPaintHorse.createAttributes().build());
        event.put(ANDALUSIAN_HORSE.get(), AndalusianHorse.createAttributes().build());
        event.put(MUSTANG_HORSE.get(), MustangHorse.createAttributes().build());
        event.put(QUARTER_HORSE.get(), QuarterHorse.createAttributes().build());
        event.put(ARABIAN_HORSE.get(), ArabianHorse.createAttributes().build());
        event.put(MORGAN_HORSE.get(), MorganHorse.createAttributes().build());
        DONKEYS.forEach(type -> event.put(type.get(), BhBreedDonkey.createAttributes().build()));
        MULES.forEach(type -> event.put(type.get(), BhBreedMule.createAttributes().build()));
        for (var group : List.of(SKELETONS, STRAYS, SCULKS)) {
            group.forEach(type -> event.put(type.get(), BhUndeadHorse.createAttributes(true).build()));
        }
        for (var group : List.of(ZOMBIES, HUSKS, DROWNEDS)) {
            group.forEach(type -> event.put(type.get(), BhUndeadHorse.createAttributes(false).build()));
        }
    }

    private static RegistryObject<EntityType<BhBreedDonkey>> registerDonkey(
            String path, RegistryObject<BreedType> breed, float width, float height) {
        return TYPES.register(path, () -> EntityType.Builder.<BhBreedDonkey>of(
                        (type, level) -> new BhBreedDonkey(type, level, breed.getKey()), MobCategory.CREATURE)
                .sized(width, height).clientTrackingRange(10).build(key(path)));
    }

    private static RegistryObject<EntityType<BhBreedMule>> registerMule(
            String path, RegistryObject<BreedType> breed, float width, float height) {
        return TYPES.register(path, () -> EntityType.Builder.<BhBreedMule>of(
                        (type, level) -> new BhBreedMule(type, level, breed.getKey()), MobCategory.CREATURE)
                .sized(width, height).clientTrackingRange(10).build(key(path)));
    }

    private static RegistryObject<EntityType<BhUndeadHorse>> registerUndead(
            String path, RegistryObject<BreedType> breed, boolean skeleton, float width, float height) {
        return TYPES.register(path, () -> EntityType.Builder.<BhUndeadHorse>of(
                        (type, level) -> new BhUndeadHorse(type, level, breed.getKey(), skeleton), MobCategory.CREATURE)
                .sized(width, height).clientTrackingRange(10).build(key(path)));
    }

    private static String key(String path) {
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path).toString();
    }

    private ModEntities() {}
}
