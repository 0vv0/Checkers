package os.checkers.network2;

import java.io.Serializable;

public enum Type implements Serializable {
    LOCAL_HOST,
    LOCAL_PORT,
    REMOTE_HOST,
    REMOTE_PORT,
    NO_PLAYER,
    SENT,
    UPDATE_POSITION;

    static boolean contains(String action) {
        for (Type a : values()) {
            if (a.name().equals(action)) {
                return true;
            }
        }
        return false;
    }
}
