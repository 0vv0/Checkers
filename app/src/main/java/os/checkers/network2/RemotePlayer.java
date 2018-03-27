package os.checkers.network2;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class RemotePlayer extends IntentService implements Handler.Callback {
    public static final String TAG = RemotePlayer.class.getName();

    //    private static final String STARTING = "starting server";
//    private static final String STARTED = "server has been started";
//    public static final String EXTRA_NAME = TAG;
//    public static final String EXTRA_REMOTE_HOST = "remote player host";
//    public static final String EXTRA_REMOTE_PORT = "remote player port";
//
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


    //    private final Server server;
    private final Handler server;

    /**
     * Target we publish for clients to send messages to our service.
     */
    private final Messenger mMessenger;

    public RemotePlayer() {
        this(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RemotePlayer(String name) {
        super(name);
        Handler mySelf = new Handler(this);
        mMessenger = new Messenger(mySelf);
        Server ser = new Server(mySelf);
        ser.start();
        server = new Handler(ser);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private Intent lastIntent;

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent.hasCategory(TAG)) {
            //our Intent - ignore
            return;
        }
        if (intent.getAction() != null) {
            this.lastIntent = intent;
        } else {
            return;
        }

        if (Action.contains(lastIntent.getAction())) {
            dispatcher(Action.valueOf(lastIntent.getAction()));
        }
    }

    private void dispatcher(Action action) {
//        switch (action) {
//            case GET_LOCALHOST:
//                getServer().getLocalHost();
//                break;
//            case SET_PLAYER:
//                try {
//                    InetAddress inetAddress = InetAddress.getByName (lastIntent.getStringExtra(EXTRA_REMOTE_HOST));
//                    int port = lastIntent.getIntExtra(EXTRA_REMOTE_PORT, 0);
////                    getServer().connect(inetAddress, port);
//                } catch (Exception ex){
//                    Log.d(TAG, "can't resolve remote host");
//                }
//                break;
//            case FREE_PLAYER:
//                getServer().restart();
//                break;
//            case UPDATE_POSITION:
////                getServer().send(lastIntent.getStringExtra(EXTRA_NAME));
//                break;
//
//        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        return false;
    }

    private Handler getServer() {
        return server;
    }

    @Override
    public void onDestroy() {
        if (getServer().getLooper().getThread().isAlive()) {
            getServer().getLooper().getThread().interrupt();
        }
        super.onDestroy();
    }
}
