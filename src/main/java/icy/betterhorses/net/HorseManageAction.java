package icy.betterhorses.net;

public enum HorseManageAction {
    WHISTLE,
    SEND_HOME,
    DISOWN,
    SET_ACTIVE;

    public static HorseManageAction fromId(int id) {
        HorseManageAction[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
