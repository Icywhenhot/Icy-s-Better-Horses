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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.LinkedHashMap;
import java.util.List;
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

    public static final List<EntityType<BhUndeadHorse>> SKELETONS =
            List.of(LARGE_SKELETON_HORSE, MEDIUM_SKELETON_HORSE, SMALL_SKELETON_HORSE);
    public static final List<EntityType<BhUndeadHorse>> ZOMBIES =
            List.of(LARGE_ZOMBIE_HORSE, MEDIUM_ZOMBIE_HORSE, SMALL_ZOMBIE_HORSE);

    //No more jankyness!!!!! YIPPYYPYPYPYPPPYPY
    public static @Nullable EntityType<? extends BhBreedHorse> forBreed(ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().get(breedKey.location());
        if (type == null) {
            return null;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(type.entityType().location()).orElse(null);
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

    private static EntityType<BhUndeadHorse> registerUndead(String path, DeferredHolder<BreedType, BreedType> breed,
                                                          boolean skeleton, float width, float height) {
        return register(path, EntityType.Builder.<BhUndeadHorse>of(
                        (type, level) -> new BhUndeadHorse(type, level, breed.getKey(), skeleton), MobCategory.CREATURE)
                .sized(width, height).eyeHeight(height * 0.95F).passengerAttachments(height * 0.90F)
                .clientTrackingRange(10).build(key(path)));
    }

    private static EntityType<BhBreedDonkey> registerDonkey(
            String path, DeferredHolder<BreedType, BreedType> breed, float width, float height) {
        return register(path, EntityType.Builder.<BhBreedDonkey>of(
                        (type, level) -> new BhBreedDonkey(type, level, breed.getKey()), MobCategory.CREATURE)
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

    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> TYPES.forEach((path, type) ->
                helper.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path), type)));
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ICELANDIC_HORSE, IcelandicHorse.createAttributes().build());
        event.put(FRIESIAN_HORSE, FriesianHorse.createAttributes().build());
        event.put(HAFLINGER_HORSE, HaflingerHorse.createAttributes().build());
        event.put(APPALOOSA_HORSE, AppaloosaHorse.createAttributes().build());
        event.put(THOROUGHBRED_HORSE, ThoroughbredHorse.createAttributes().build());
        event.put(AMERICAN_PAINT_HORSE, AmericanPaintHorse.createAttributes().build());
        event.put(ANDALUSIAN_HORSE, AndalusianHorse.createAttributes().build());
        event.put(MUSTANG_HORSE, MustangHorse.createAttributes().build());
        event.put(QUARTER_HORSE, QuarterHorse.createAttributes().build());
        event.put(ARABIAN_HORSE, ArabianHorse.createAttributes().build());
        event.put(MORGAN_HORSE, MorganHorse.createAttributes().build());
        event.put(PERCHERON_HORSE, PercheronHorse.createAttributes().build());
        event.put(SHIRE_HORSE, ShireHorse.createAttributes().build());
        event.put(BELGIAN_HORSE, BelgianHorse.createAttributes().build());
        event.put(CLYDESDALE_HORSE, ClydesdaleHorse.createAttributes().build());
        for (EntityType<BhBreedDonkey> donkey : DONKEYS) {
            event.put(donkey, BhBreedDonkey.createAttributes().build());
        }
        for (EntityType<BhUndeadHorse> horse : SKELETONS) event.put(horse, BhUndeadHorse.createAttributes(true).build());
        for (EntityType<BhUndeadHorse> horse : ZOMBIES) event.put(horse, BhUndeadHorse.createAttributes(false).build());
    }

    private static <T extends Entity> EntityType<T> register(
            String path, EntityType<T> type) {
        TYPES.put(path, type);
        return type;
    }

    private static String key(String path) {
        return ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path).toString();
    }

    private ModEntities() {}
}
