package os.checkers.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NsdService extends IntentService {
    public final static String TAG = NsdService.class.getName();
    public final static String POSITION = "position";
    public final static String PLAYERS = "players";

    private volatile static NsdHelper mNsdHelper;
    private volatile static Connection mConnection;
    private volatile static Handler mHandler;

    public NsdService() {
        this(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NsdService(String name) {
        super(name);
        Log.d(TAG, "Starting...");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart...");
        if (mHandler == null) {
            mHandler = new Handler(getCallback());
        }
        if (mConnection == null) {
            mConnection = new Connection(mHandler);
        }
        if (mNsdHelper == null) {
            NsdManager nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            mNsdHelper = new NsdHelper(nsdManager, mHandler);
        }
    }

    private void registerService() {
        if (mConnection != null && mNsdHelper != null) {
            Log.d(TAG, "Socket on local port ready");
            Log.d(TAG, "Local port is " + String.valueOf(mConnection.getLocalPort()));
            mNsdHelper.registerService(mConnection.getLocalPort());
        }
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d(TAG, "" + intent.getAction());
        String action = intent.getAction();
        if (action != null) {
            switch (IntentActions.valueOf(action)) {
                case REQUEST_PLAYERS_LIST:
                    if (mNsdHelper != null) {
                        mNsdHelper.discoverServices();
                    }
                    break;
                case SEND_POSITION:
                    if(mConnection!=null){
                        mConnection.sendMessage(intent.getStringExtra(POSITION));
                    }
                    break;
                case CONNECT:
                    if(mConnection!=null&&mNsdHelper!=null&&mNsdHelper.getChosenServiceInfo()!=null){
                        mConnection.connectToServer(mNsdHelper.getChosenServiceInfo().getHost(), mNsdHelper.getChosenServiceInfo().getPort());
                    }
                    break;
            }
        }
    }

    private Handler.Callback getCallback() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle msgData = msg.getData();
                if (msgData.containsKey(Connection.TAG)) {
                    switch (msgData.getString(Connection.TAG)) {
                        case Connection.PORT:
                            registerService();
                            break;
                    }
                } else if (msgData.containsKey(POSITION)) {
                    sendIntentWithPosition(msgData.getString(POSITION));
                } else if (msgData.containsKey(NsdHelper.TAG)) {
                    switch (msgData.getString(NsdHelper.TAG)) {
                        case NsdHelper.RESOLVED:
                            Log.d(TAG, "Service resolved...");
                            sendIntent(IntentActions.LIST_PLAYERS.name(), PLAYERS, mNsdHelper.getChosenServiceInfo().toString());
                            break;
                        case NsdHelper.REGISTERED:
                            Log.d(TAG, "Service registered...");
                            break;
                    }
                }
                return false;
            }
        };
    }

    private void sendIntent(String action, String name, String value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(name, value);
        sendBroadcast(intent);
    }

    private void sendIntentWithPosition(String position) {
        Log.d(TAG, "Sending position via broadcast...");
        sendIntent(IntentActions.SET_POSITION.name(), POSITION, position);
    }

    public static void exit() {
        if (mConnection != null) {
            mConnection.tearDown();
            mConnection = null;
        }
        if (mNsdHelper != null) {
            mNsdHelper.tearDown();
            mNsdHelper = null;
        }
        if (mHandler != null) {
            mHandler = null;
        }


    }
}
