package icy.betterhorses.net;

import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class BhHorseKind {

    public static final TagKey<EntityType<?>> MANAGED = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "horses"));

    private static @Nullable Set<ResourceLocation> breedEntities;

    private BhHorseKind() {}

    public static boolean managed(@Nullable Entity entity) {
        return entity instanceof AbstractHorse horse && managed(horse.getType());
    }

    public static boolean managed(EntityType<?> type) {
        return type.is(MANAGED) || registeredBreed(type);
    }

    public static boolean managedId(@Nullable String id) {
        ResourceLocation parsed = id == null || id.isEmpty() ? null : ResourceLocation.tryParse(id);
        EntityType<?> type = parsed == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).orElse(null);
        return type != null && managed(type);
    }

    private static boolean registeredBreed(EntityType<?> type) {
        if (breedEntities == null) {
            Set<ResourceLocation> ids = new HashSet<>();
            for (BreedType breed : BhRegistries.breedTypeRegistry()) {
                ids.add(breed.entityType().location());
            }
            breedEntities = ids;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return id != null && breedEntities.contains(id);
    }
}
