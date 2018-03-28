package os.checkers.network2;

enum Action {
    GET_LOCALHOST,
    SET_PLAYER,
    FREE_PLAYER,
    UPDATE_POSITION;

    static boolean contains(String action) {
        for (Action a : values()) {
            if (a.name().equals(action)) {
                return true;
            }
        }
        return false;
    }
}
