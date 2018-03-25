package os.checkers.network;

public enum IntentActions {
    REQUEST_PLAYERS_LIST,
    LIST_PLAYERS1,
    WIFI_ERROR,
    SET_POSITION,
    GET_POSITION,
    SEND_POSITION,
    CONNECT;
    public static final String STOP = "stop";
    public static final String SEARCH = "start searching players";
    public static final String SEND = "send position";

    static boolean containsName(String name) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
