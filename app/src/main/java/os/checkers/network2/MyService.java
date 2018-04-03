package os.checkers.network2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyService extends Service implements Handler.Callback {
    private static final String TAG = MyService.class.getName();
    private MyHandler inHandler;

    private Server server;
    private InetSocketAddress remote;
    private Socket socket;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inHandler = new MyHandler(this);
        server = new Server(inHandler);
        server.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what < 0 || msg.what >= HandlerType.values().length) {
            return false;
        }
        HandlerType type = HandlerType.values()[msg.what];
        switch (type) {
            case REMOTE_PLAYER:
                remote = (InetSocketAddress) msg.getData().getSerializable(type.name());
                break;
            case NO_PLAYER:
                break;
            case CONNECTION_REQUEST:
                InetSocketAddress address = (InetSocketAddress) msg.getData().getSerializable(type.name());
                if (address != null) {
                    if (remote != null) {
                        if (address.getAddress().getHostAddress().equals(remote.getAddress().getHostAddress())) {

                        }
                    } else {
                        remote = address;
                        try {
                            socket = new Socket(remote.getAddress(), remote.getPort());
                            new Connection(new MyHandler(this)).receiveFrom(socket);
                        } catch (IOException e) {
                            Log.d(TAG, "Cannot open Socket to " + remote);
                        }

                    }
                }
                break;
            case SENT:
                break;
            case ERROR:
                break;
            case UPDATE_POSITION:
                Intent intent = new Intent();
                intent.setAction(IntentAction.UPDATE_POSITION.name());
                intent.addCategory(TAG);
                sendBroadcast(intent);
                break;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            if (server.isAlive()) {
                server.interrupt();
            }
            server = null;
        }
        super.onDestroy();
    }
}
