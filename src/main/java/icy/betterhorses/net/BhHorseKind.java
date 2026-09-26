package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;

// Llamas/trader llamas are AbstractHorse subclasses too, so anything gated only on `instanceof
// AbstractHorse` picks them up by accident. Gate ownership/tracking on this tag instead.
public final class BhHorseKind {

    public static final TagKey<EntityType<?>> MANAGED = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "horses"));

    private BhHorseKind() {}

    public static boolean managed(@Nullable Entity entity) {
        return entity instanceof AbstractHorse horse && managed(horse.getType());
    }

    public static boolean managed(EntityType<?> type) {
        return type.is(MANAGED);
    }
}
