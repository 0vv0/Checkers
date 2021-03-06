package os.checkers.network2;

import java.io.Serializable;

public enum HandlerType implements Serializable {
    LOCAL_PORT,
    REMOTE_PLAYER,
    NO_PLAYER,
    CONNECTION_REQUEST,
    SENT,
    ERROR,
    UPDATE_POSITION;

    static boolean contains(String action) {
        for (HandlerType a : values()) {
            if (a.name().equals(action)) {
                return true;
            }
        }
        return false;
    }
}
