package icy.betterhorses.net.client;

import java.util.ArrayList;
import java.util.List;

public final class BhClientCaches {

    private static final List<Runnable> RESETS = new ArrayList<>();

    private BhClientCaches() {}

    public static void register(Runnable reset) {
        RESETS.add(reset);
    }

    public static void resetAll() {
        for (Runnable reset : RESETS) {
            reset.run();
        }
    }
}
