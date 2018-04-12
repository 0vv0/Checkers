package os.checkers.network;

public interface TCPConnectionListener {
    void onReady(TCPConnection tcpConnection);
    void onError(TCPConnection tcpConnection, Exception e);
    void onDisconnect(TCPConnection tcpConnection);
    void onReceive(TCPConnection tcpConnection, String dataString);
}
