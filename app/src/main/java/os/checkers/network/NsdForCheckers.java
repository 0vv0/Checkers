package os.checkers.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


public class NsdForCheckers extends IntentService {
    public static final String TAG = NsdForCheckers.class.getName();
    private NsdManager mNsdManager;
    private Discovery mDiscoveryListener;

    public NsdForCheckers() {
        this(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NsdForCheckers(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        String action = intent.getAction();
        if (IntentActions.SEARCH.equals(action)) {
            mNsdManager.discoverServices(NsdHelperStringLiterals.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        } else if (IntentActions.STOP.equals(action)) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            this.stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        mDiscoveryListener = getDiscovery();
    }

    private Discovery getDiscovery() {
        return Discovery.getInstance(new DiscoveryResolver() {
            @Override
            public void onResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolved to " + serviceInfo);
                Intent intent = new Intent(Action.ADD_PLAYER);
                intent.addCategory(TAG);
                intent.putExtra(Action.ADD_PLAYER, serviceInfo);
                sendBroadcast(intent);
            }

            @Override
            public void onLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Lost the " + serviceInfo);
                Intent intent = new Intent(Action.REMOVE_PLAYER);
                intent.addCategory(TAG);
                intent.putExtra(Action.REMOVE_PLAYER, serviceInfo);
                sendBroadcast(intent);
            }
        });
    }

    public static class Action {
        public static final String ADD_PLAYER = "add";
        public static final String REMOVE_PLAYER = "remove";
    }
}
