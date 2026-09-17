package icy.betterhorses.net;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BhHorseAttributes {

    public enum Source {
        BOND, ABILITY, ARCHETYPE, GEAR
    }

    private static final Map<String, UUID> IDS = new ConcurrentHashMap<>();

    private BhHorseAttributes() {}

    public static void apply(LivingEntity target, Attribute attr, Source src, String key,
                             double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = target.getAttribute(attr);
        if (inst == null) {
            return;
        }
        String name = nameFor(src, key);
        inst.removeModifier(uuidFor(name));
        if (amount != 0.0D) {
            inst.addTransientModifier(new AttributeModifier(uuidFor(name), name, amount, op));
        }
    }

    public static void clear(LivingEntity target, Attribute attr, Source src, String key) {
        AttributeInstance inst = target.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(uuidFor(nameFor(src, key)));
        }
    }

    private static String nameFor(Source src, String key) {
        return IcysBetterHorses.RESOURCE_NAMESPACE + ":" + src.name().toLowerCase(Locale.ROOT) + "/" + key;
    }

    private static UUID uuidFor(String name) {
        return IDS.computeIfAbsent(name, n -> UUID.nameUUIDFromBytes(n.getBytes(StandardCharsets.UTF_8)));
    }
}
