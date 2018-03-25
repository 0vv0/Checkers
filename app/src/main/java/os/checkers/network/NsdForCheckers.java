package os.checkers.network;

import android.app.IntentService;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.Process;
import android.util.Log;

import java.io.Serializable;


public class NsdForCheckers extends Service {
    public static final String TAG = NsdForCheckers.class.getName();

    private static volatile Thread mThread;

//    public NsdForCheckers() {
//        this(TAG);
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//
//    /**
//     * Creates an IntentService.  Invoked by your subclass's constructor.
//     *
//     * @param name Used to name the worker thread, important only for debugging.
//     */
//    public NsdForCheckers(String name) {
//        super(name);
//    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
////        mNsdManager = (NsdManager)getSystemService(NSD_SERVICE);
//    }

//    @Override
//    protected void onHandleIntent(final Intent intent) {
//        if (!intent.hasCategory(TAG)) {
//            return;
//        }
//        String action = intent.getAction();
//        if (IntentActions.SEARCH.equals(action)) {
//            if (mThread == null) {
//                synchronized (Discoverer.class) {
//                    mThread = new Discoverer();
//                }
//                mThread.start();
//            }
//        } else if (IntentActions.STOP.equals(action)) {
//            if (mThread != null && mThread.isAlive()) {
//                Log.d(TAG, "interrupting discoverer");
//                mThread.interrupt();
//                mThread = null;
//            }
//        }
//    }

    private void sendIntent(String action, Parcelable info) {
        Intent intent = new Intent(action);
        intent.addCategory(TAG);
        intent.putExtra(action, info);
        sendBroadcast(intent);
    }

    public static class Action {
        public static final String ADD_PLAYER = "add";
        public static final String REMOVE_PLAYER = "remove";
    }

    private class Discoverer extends Thread {
        private NsdManager mNsdManager;
        private Discovery mDiscoveryListener;
//
//        Discoverer() {
//            super(TAG);
//            Log.d(TAG, "Discoverer has been created");
//        }

        @Override
        public void run() {
            try {
                Log.d(TAG, " running in " + Thread.currentThread().getName());
                mDiscoveryListener = new Discovery() {
                    @Override
                    public void onServiceLost(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Lost the " + serviceInfo);
                        sendIntent(Action.REMOVE_PLAYER, serviceInfo);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Resolved to " + serviceInfo);
                        sendIntent(Action.ADD_PLAYER, serviceInfo);
                    }
                };
                Log.d(TAG, mDiscoveryListener.toString());
                mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
                Log.d(TAG, mNsdManager.toString());
                mNsdManager.discoverServices(NsdHelperStringLiterals.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            } catch (Exception ex) {
                Log.d(TAG, ex.toString());
                try {
                    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            } finally {
                mNsdManager = null;
                mDiscoveryListener = null;
                mThread = null;
            }
        }

        @Override
        public synchronized void interrupt() {
            Log.d(TAG, Thread.currentThread().getName() + " has been interrupted");
            super.interrupt();
        }
    }
}
