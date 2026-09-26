package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BreedCoatSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class BhNamedCoats {

    private static final Map<ResourceLocation, Boolean> found = new HashMap<>();

    private BhNamedCoats() {}

    public static void clearCache() {
        found.clear();
    }

    public static ResourceLocation coat(BhBreedHorse horse) {
        BreedCoatSet coats = horse.bhCoatSet();
        ResourceLocation normal = coats.texture(horse.bhCoat(), horse.isBaby());
        Component custom = horse.getCustomName();
        if (custom == null) {
            return normal;
        }

        String name = slug(custom.getString());
        if (name.isEmpty()) {
            return normal;
        }

        String folder = horse.isBaby() && coats.hasFoalVariant() ? coats.folder() + "/baby" : coats.folder();
        ResourceLocation named = ResourceLocation.tryBuild(coats.resourceNamespace(),
                "textures/entity/horse/" + folder + "/named/" + name + ".png");
        if (named == null) {
            return normal;
        }
        boolean ok = found.computeIfAbsent(named, id -> Minecraft.getInstance()
                .getResourceManager().getResource(id).isPresent());
        return ok ? named : normal;
    }

    private static String slug(String raw) {
        String plain = ChatFormatting.stripFormatting(raw);
        if (plain == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(plain.length());
        for (char c : plain.trim().toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') {
                out.append('_');
            } else if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.') {
                out.append(c);
            }
        }
        return out.toString();
    }
}
