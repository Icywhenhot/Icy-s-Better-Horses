package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;

import java.util.Locale;

/**
 * Telemetry for the jump animation.
 *
 * <p>Off by default. The "Jump Animation Debug" key cycles OFF -> EVENTS -> EVENTS_AND_HUD:
 *
 * <ul>
 *   <li>{@code EVENTS} logs one line per takeoff / apex / touchdown / settle to the game log.</li>
 *   <li>{@code EVENTS_AND_HUD} additionally draws the live weights over the crosshair, so a
 *       screen recording carries its own numbers and a frame of a GIF can be read directly.</li>
 * </ul>
 *
 * <p>The HUD follows one horse at a time - whichever jumped most recently - because two horses
 * writing to one panel is unreadable.
 */
public final class BhJumpDebug {

    public static final int OFF = 0;
    public static final int EVENTS = 1;
    public static final int EVENTS_AND_HUD = 2;

    private static int level = OFF;

    private static int hudEntityId = -1;
    private static String hudLine1 = "";
    private static String hudLine2 = "";
    private static String hudLine3 = "";
    private static long hudStamp = 0L;

    /** Live for four seconds after the last update, so the panel clears itself. */
    private static final long HUD_LINGER_MILLIS = 4000L;
    private static final long BANNER_MILLIS = 2500L;

    private static String banner = "";
    private static long bannerStamp = 0L;

    private BhJumpDebug() {
    }

    public static int cycle() {
        level = (level + 1) % 3;
        if (level == OFF) {
            hudEntityId = -1;
        }
        banner(switch (level) {
            case EVENTS -> "jump debug: log only (press again for HUD)";
            case EVENTS_AND_HUD -> "jump debug: log + HUD (press again for off)";
            default -> "jump debug: off";
        });
        return level;
    }

    /**
     * A short message shown in the panel for a couple of seconds. The action bar moved in 26.2
     * and this panel already exists, so dev toggles report through it rather than chasing the
     * new API for two strings.
     */
    public static void banner(String message) {
        banner = message;
        bannerStamp = System.currentTimeMillis();
        IcysBetterHorses.LOGGER.info("[jump] {}", message);
    }

    public static int level() {
        return level;
    }

    public static boolean logging() {
        return level >= EVENTS;
    }

    public static boolean hud() {
        return level >= EVENTS_AND_HUD;
    }

    public static void takeoff(int entityId, float verticalSpeed, float gather,
                               float launchPower, boolean ridden) {
        if (!logging()) {
            return;
        }
        hudEntityId = entityId;
        IcysBetterHorses.LOGGER.info(
                "[jump] #{} TAKEOFF vy={} gather={} launch={} ridden={}",
                entityId, f(verticalSpeed), f(gather), f(launchPower), ridden);
    }

    /** Logged once per flight, the frame the vertical speed crosses zero. */
    public static void apex(int entityId, float airSeconds, float peakRise) {
        if (!logging()) {
            return;
        }
        IcysBetterHorses.LOGGER.info("[jump] #{} APEX  after={}s peakVy={}",
                entityId, f(airSeconds), f(peakRise));
    }

    /**
     * @param handoffSeconds the frame delta the impact clock started on. This is the number that
     *                       proves there is no gap between flight and landing: the impact curve
     *                       begins at 0 on this frame and the reach weight is still carrying the
     *                       flight pose, so the two overlap rather than cut.
     */
    public static void touchdown(int entityId, float airSeconds, float fallSpeed,
                                 float impactPower, float carriedReach, float handoffSeconds) {
        if (!logging()) {
            return;
        }
        hudEntityId = entityId;
        IcysBetterHorses.LOGGER.info(
                "[jump] #{} LAND  air={}s vy={} power={} carriedReach={} handoff={}ms",
                entityId, f(airSeconds), f(fallSpeed), f(impactPower), f(carriedReach),
                f(handoffSeconds * 1000.0F));
    }

    public static void settled(int entityId, float totalSeconds) {
        if (!logging()) {
            return;
        }
        IcysBetterHorses.LOGGER.info("[jump] #{} DONE  total={}s", entityId, f(totalSeconds));
    }

    /** Called every frame from the gait while any jump weight is non-zero. */
    public static void sample(int entityId, BhHorseRenderState state, float airSeconds) {
        if (!hud() || (hudEntityId != -1 && hudEntityId != entityId)) {
            return;
        }
        hudEntityId = entityId;
        hudLine1 = String.format(Locale.ROOT,
                "#%d  vy %s  air %ss  ground %s",
                entityId, f(state.verticalSpeed), f(airSeconds), state.onGround ? "Y" : "n");
        hudLine2 = String.format(Locale.ROOT,
                "gather %s  thrust %s  flight %s",
                f(state.jumpGather), f(state.jumpThrust), f(state.jumpFlight));
        hudLine3 = String.format(Locale.ROOT,
                "rise %s  fall %s  reach %s  hit %s/%s",
                f(state.jumpRise), f(state.jumpFall), f(state.jumpReach),
                f(state.jumpImpact), f(state.jumpImpactSecond));
        hudStamp = System.currentTimeMillis();
    }

    public static String[] hudLines() {
        long now = System.currentTimeMillis();
        boolean showBanner = now - bannerStamp <= BANNER_MILLIS;
        boolean showTelemetry = hud() && now - hudStamp <= HUD_LINGER_MILLIS;

        if (showBanner && showTelemetry) {
            return new String[] {banner, hudLine1, hudLine2, hudLine3};
        }
        if (showBanner) {
            return new String[] {banner};
        }
        if (showTelemetry) {
            return new String[] {hudLine1, hudLine2, hudLine3};
        }
        return null;
    }

    public static void reset() {
        hudEntityId = -1;
        hudStamp = 0L;
        bannerStamp = 0L;
    }

    private static String f(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
