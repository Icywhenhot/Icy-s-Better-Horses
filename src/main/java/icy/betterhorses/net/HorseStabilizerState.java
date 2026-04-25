package icy.betterhorses.net;

public enum HorseStabilizerState {
    CLOSED,
    HALF_OPEN,
    OPEN;

    public static HorseStabilizerState fromId(int id) {
        HorseStabilizerState[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
