package icy.betterhorses.net;

public enum HorseCommand {
    FOLLOW,
    STAY,
    RETURN_HOME,
    SET_HOME,
    WANDER,
    ABILITY;

    public static boolean toggleable(HorseBreed breed) {
        return switch (breed) {
            case APPALOOSA -> true;
            default -> false;
        };
    }

    public static HorseCommand fromId(int id) {
        HorseCommand[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
