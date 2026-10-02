package icy.betterhorses.net.client;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class HccThrottle {

    private static final List<Field> throttles = ModList.get().isLoaded("horsecombatcontrols") ? find() : List.of();

    private static boolean wasDriving;

    private HccThrottle() {}

    public static void tick(@Nullable AbstractHorse horse, boolean driving, boolean forwardHeld) {
        if (throttles.isEmpty()) return;
        if (horse != null && wasDriving && !driving && !forwardHeld) {
            for (Field f : throttles) {
                try {
                    f.setDouble(horse, 0.0D);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        wasDriving = horse != null && driving;
    }

    private static List<Field> find() {
        List<Field> out = new ArrayList<>();
        for (Field f : AbstractHorse.class.getDeclaredFields()) {
            if (f.getType() == double.class && f.getName().contains("prevSpeedPercent")) {
                f.setAccessible(true);
                out.add(f);
            }
        }
        return out;
    }
}
