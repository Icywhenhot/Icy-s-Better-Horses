package icy.betterhorses.net;

public enum HorseCommand {
    FOLLOW,
    STAY,
    RETURN_HOME,
    SET_HOME;

    public static HorseCommand fromId(int id) {
        HorseCommand[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
