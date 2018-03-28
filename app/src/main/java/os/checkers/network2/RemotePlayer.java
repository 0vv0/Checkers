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

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent.hasCategory(TAG)) {
            //our Intent - ignore
            return;
        }
        if (intent.getAction() != null) {
            if (IntentAction.contains(intent.getAction())) {
                dispatch(intent);
            }
        }
    }

    private void dispatch(final Intent intent) {
        IntentAction action = IntentAction.valueOf(intent.getAction());
        switch (action) {
            case GET_LOCALHOST:
                getServer().addRequest(HandlerType.LOCAL_HOST, "");
                getServer().addRequest(HandlerType.LOCAL_PORT, "");
                break;
            case SET_PLAYER:
                String inetAddress = intent.getStringExtra(HandlerType.REMOTE_HOST.name());
                getServer().addRequest(HandlerType.REMOTE_HOST, inetAddress);
                int port = intent.getIntExtra(HandlerType.REMOTE_PORT.name(), 0);
                getServer().addRequest(HandlerType.REMOTE_PORT, String.valueOf(port));
                break;
            case FREE_PLAYER:
                getServer().interrupt();
                break;
            case UPDATE_POSITION:
                getServer().addRequest(HandlerType.UPDATE_POSITION, intent.getStringExtra(IntentAction.UPDATE_POSITION.name()));
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

    private void sendIntent(HandlerType type, String msg){
        sendIntent(type.name(), msg);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what>=0&&msg.what<HandlerType.values().length){
            HandlerType type = HandlerType.values()[msg.what];
            sendIntent(type, msg.getData().getString(type.name()));
        }
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
