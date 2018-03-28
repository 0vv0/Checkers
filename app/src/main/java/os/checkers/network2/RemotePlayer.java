package os.checkers.network2;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class RemotePlayer extends IntentService implements Handler.Callback {
    public static final String TAG = RemotePlayer.class.getName();

    private volatile Server server;

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
        mMessenger = new Messenger(new Handler(this));
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
            if (Action.contains(lastIntent.getAction())) {
                dispatcher(Action.valueOf(lastIntent.getAction()));
            }
        }
    }

    private void dispatcher(Action action) {
        switch (action) {
            case GET_LOCALHOST:
                getServer().getLocalHost();
                break;
            case SET_PLAYER:
                String inetAddress = lastIntent.getStringExtra(Type.REMOTE_HOST.name());
                int port = lastIntent.getIntExtra(Type.REMOTE_PORT.name(), 0);
                getServer().connectWith(inetAddress, port);
                break;
            case FREE_PLAYER:
                getServer().freePlayer();
                break;
            case UPDATE_POSITION:
                getServer().sendPosition(lastIntent.getStringExtra(Action.UPDATE_POSITION.name()));
                break;
        }
    }

    private void dispatchType(final Message msg){
        Type type = Type.values()[msg.what];
        switch (type){
            case LOCAL_HOST:
                sendIntent("", msg.getData().getString(Type.LOCAL_HOST.name()));
                break;
            case LOCAL_PORT:
                break;
            case REMOTE_HOST:
                break;
            case REMOTE_PORT:
                break;
            case NO_PLAYER:
                break;
            case SENT:
                break;
            case UPDATE_POSITION:

                break;
        }
    }

    private void sendIntent(String action, String msgString){
        Intent intent = new Intent();
        intent.addCategory(TAG);
        intent.setAction(action);
        intent.putExtra(TAG, msgString);
        sendBroadcast(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        dispatchType(msg);
        return false;
    }

    private synchronized Server getServer() {
        if (server == null || !server.isAlive()) {
            synchronized (this) {
                server = new Server(new Handler(this));
                server.start();
            }
        }
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
