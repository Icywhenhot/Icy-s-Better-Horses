package icy.betterhorses.net;

import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class BhSiegeTow {

    private static final ClassValue<Boolean> TURNS_ITSELF = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Method m : type.getDeclaredMethods()) {
                if (m.getName().equals("applyRotations") && m.getParameterCount() == 6 && !m.isBridge()) {
                    return true;
                }
            }
            return false;
        }
    };

    private static final double MANTLET_SPEED = 0.07D;

    private BhSiegeTow() {}

    public static boolean turnsItself(Class<?> renderer) {
        return TURNS_ITSELF.get(renderer);
    }

    public static boolean towing(@Nullable Entity siege) {
        return siege != null
                && siege.getFirstPassenger() instanceof Horse
                && BuiltInRegistries.ENTITY_TYPE.getKey(siege.getType()).getNamespace().equals("kingdomsieges");
    }

    public static double base(Entity siege, double horseSpeed) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(siege.getType()).toString().equals("kingdomsieges:mantlet")
                ? MANTLET_SPEED : horseSpeed;
    }

    public static double pull(AbstractHorse horse, double base) {
        ArchetypeType type = BhBreedData.of(IHorseData.of(horse).bh_getBreedKey()).archetype();
        double scale = 1.0D;
        if (type == BhContent.DRAFT.get()) {
            scale = 2.5D;
        } else if (type == BhContent.WAR.get()) {
            scale = 1.5D;
        } else if (type == BhContent.WESTERN.get()) {
            scale = 1.2D;
        }
        return base / 2.0D * scale;
    }

    public static float stride(Entity siege) {
        return (float) Mth.length(siege.getX() - siege.xo, 0.0D, siege.getZ() - siege.zo);
    }

    public static void face(AbstractHorse horse) {
        Entity siege = horse.getVehicle();
        if (!towing(siege)) return;
        float yaw = siege.getYRot();
        horse.setYRot(yaw);
        horse.yRotO = siege.yRotO;
        horse.setYBodyRot(yaw);
        horse.yBodyRotO = siege.yRotO;
        horse.setYHeadRot(yaw);
        horse.yHeadRotO = siege.yRotO;
    }
}
