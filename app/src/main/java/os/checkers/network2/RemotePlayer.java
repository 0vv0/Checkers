package os.checkers.network2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class RemotePlayer extends Service implements Handler.Callback {
    public static final String TAG = RemotePlayer.class.getName();
    private final IBinder mBinder = new LocalBinder();
    private Server server;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(server==null||!server.isAlive()){
            Handler handler = new Handler(getMainLooper(), this);
            server = new Server(handler);
//        server.connect();
            server.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean handleMessage(Message msg) {


        return false;
    }


    @Override
    public void onDestroy() {
        if(server!=null&&server.isAlive()){
            server.interrupt();
            server = null;
        }
        super.onDestroy();
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        RemotePlayer getService() {
            // Return this instance of LocalService so clients can call public methods
            return RemotePlayer.this;
        }
    }
}
