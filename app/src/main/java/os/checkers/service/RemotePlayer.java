package os.checkers.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import os.checkers.network.TCPConnection;
import os.checkers.network.TCPConnectionListener;

public class RemotePlayer extends Service implements TCPConnectionListener {
    public RemotePlayer() {
        binder = new Binder();
    }

    private IBinder binder;
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onReady(TCPConnection tcpConnection) {

    }

    @Override
    public void onError(TCPConnection tcpConnection, Exception e) {

    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {

    }

    @Override
    public void onReceive(TCPConnection tcpConnection, String dataString) {

    }
}
