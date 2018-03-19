package os.checkers.network;

public enum IntentActions {
    REQUEST_PLAYERS_LIST,
    LIST_PLAYERS,
    WIFI_ERROR,
    SET_POSITION,
    GET_POSITION;
    static boolean containsName(String name) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
