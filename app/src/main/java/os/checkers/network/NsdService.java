package os.checkers.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
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
        final Intent intent = new Intent();
        mHandler = new Handler(this.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.getData().containsKey(Connection.TAG)) {
                    mNsdHelper.registerService(mConnection.getLocalPort());
                } else if (msg.getData().containsKey(POSITION)) {
                    intent.setAction(IntentActions.SET_POSITION.name());
                    intent.putExtra(POSITION, msg);
                    sendBroadcast(intent);
                }
                return false;
            }
        });
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(mNsdHelper==null){
            NsdManager nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            mNsdHelper = new NsdHelper(nsdManager, mHandler);
        }
        if (mConnection == null) {
            mConnection = new Connection(mHandler);
        }
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d(TAG, "" + intent.getAction());
        if(mConnection!=null) {
            mNsdHelper.discoverServices();
        }
    }


}
