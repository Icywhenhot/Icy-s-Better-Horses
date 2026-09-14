package icy.betterhorses.net;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class BhHorseAttributes {

    public enum Source {
        BOND, ABILITY, ARCHETYPE, GEAR
    }

    private BhHorseAttributes() {}

    public static void apply(LivingEntity target, Holder<Attribute> attr, Source src, String key,
                             double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = target.getAttribute(attr);
        if (inst == null) {
            return;
        }
        ResourceLocation id = idFor(src, key);
        inst.removeModifier(id);
        if (amount != 0.0D) {
            inst.addTransientModifier(new AttributeModifier(id, amount, op));
        }
    }

    public static void clear(LivingEntity target, Holder<Attribute> attr, Source src, String key) {
        AttributeInstance inst = target.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(idFor(src, key));
        }
    }

    private static ResourceLocation idFor(Source src, String key) {
        return ResourceLocation.fromNamespaceAndPath(
                IcysBetterHorses.RESOURCE_NAMESPACE, src.name().toLowerCase() + "/" + key);
    }
}
