package os.checkers.network2;

import java.io.Serializable;

public class Player implements Serializable{
    String hostName;
    int hostPort;

    public Player(String hostName, int hostPort) {
        this.hostName = hostName;
        this.hostPort = hostPort;
    }

    public String getHostName() {
        return hostName;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof Player
                && ((Player) obj).hostName.equals(hostName);
    }

}
