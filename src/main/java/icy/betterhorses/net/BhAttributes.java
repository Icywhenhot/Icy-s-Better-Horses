package icy.betterhorses.net;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class BhAttributes {

    public static final Attribute STEP_HEIGHT_ADDITION = register("step_height_addition",
            new RangedAttribute("attribute.name.icys-better-horses.step_height_addition",
                    0.0D, -512.0D, 512.0D).setSyncable(true));

    public static final Attribute SWIM_SPEED = register("swim_speed",
            new RangedAttribute("attribute.name.icys-better-horses.swim_speed",
                    1.0D, 0.0D, 1024.0D).setSyncable(true));

    private static Attribute register(String path, Attribute attribute) {
        return Registry.register(BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path), attribute);
    }

    public static void register() {}

    private BhAttributes() {}
}
