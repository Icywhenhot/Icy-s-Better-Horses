package icy.betterhorses.net;

/** What the horse management screen can do to a horse. */
public enum HorseManageAction {
    WHISTLE,
    SEND_HOME,
    DISOWN,
    /** Makes this the horse the whistle keybind calls, until the player picks another. */
    SET_ACTIVE;

    public static HorseManageAction fromId(int id) {
        HorseManageAction[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
