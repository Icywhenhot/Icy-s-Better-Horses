package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BreedCoatSet;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class BhBreedCarpets {

    private static final Map<Identifier, Boolean> found = new HashMap<>();

    private BhBreedCarpets() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "breed_carpets");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        found.clear();
                    }
                });
    }

    public static @Nullable Identifier carpet(BhBreedHorse horse, DyeColor color) {
        BreedCoatSet coats = horse.bhCoatSet();
        Identifier texture = Identifier.tryBuild(coats.resourceNamespace(),
                "textures/entity/horse/" + coats.folder() + "/carpet/" + color.getName() + ".png");
        if (texture == null) {
            return null;
        }
        boolean ok = found.computeIfAbsent(texture, id -> Minecraft.getInstance()
                .getResourceManager().getResource(id).isPresent());
        return ok ? texture : null;
    }
}
