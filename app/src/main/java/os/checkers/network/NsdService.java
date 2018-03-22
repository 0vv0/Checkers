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

    private NsdHelper mNsdHelper;
    private Connection mConnection;
    private Handler mHandler;

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

    private void sendBroadcastWithPosition(String position) {
        Log.d(TAG, "Sending position via broadcast...");
        Intent intent = new Intent();
        intent.setAction(IntentActions.SET_POSITION.name());
        intent.putExtra(POSITION, position);
        sendBroadcast(intent);
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
            }
        }
    }

    private Handler.Callback getCallback(){
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
                    sendBroadcastWithPosition(msgData.getString(POSITION));
                } else if (msgData.containsKey(NsdHelper.TAG)) {
                    switch (msgData.getString(NsdHelper.TAG)) {
                        case NsdHelper.RESOLVED:
                            Log.d(TAG, "Service resolved...");
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

    @Override
    protected void finalize() throws Throwable {
        try {
            if(mNsdHelper!=null){
                mNsdHelper.finalize();
            }
            if(mConnection!=null){
                mConnection.finalize();
            }
        }finally {
            super.finalize();
        }
    }
}
