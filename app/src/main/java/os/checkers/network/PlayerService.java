package os.checkers.network;

import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class PlayerService extends Service {
    public static final String TAG = PlayerService.class.getName();

    private static volatile ServiceHandler mServiceHandler;
    private static Discovery discovery;
    private static boolean continueDiscovery = true;

    private Discovery getDiscovery() {
        return new Discovery() {
            @Override
            public void onDiscoveryStopped(String nsdService) {
                super.onDiscoveryStopped(nsdService);
                Log.d(TAG, "discovery stopped");
                discovery = null;
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Lost the " + serviceInfo);
                if (continueDiscovery) {
                    sendIntent(Action.REMOVE_PLAYER, serviceInfo);
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolved to " + serviceInfo);
                if (continueDiscovery) {
                    sendIntent(Action.ADD_PLAYER, serviceInfo);
                }
            }
        };
    }

    private void sendIntent(String action, Parcelable info) {
        Intent intent = new Intent(action);
        intent.addCategory(TAG);
        intent.putExtra(action, info);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate in " + Thread.currentThread().getName());
        if (mServiceHandler == null) {
            // Start up the thread running the service.  Note that we create a
            // separate thread because the service normally runs in the process's
            // main thread, which we don't want to block.  We also make it
            // background priority so CPU-intensive work will not disrupt our UI.
            HandlerThread thread = new HandlerThread(TAG,
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();

            // Get the HandlerThread's Looper and use it for our Handler
            mServiceHandler = new ServiceHandler(thread.getLooper());
        }
        Log.d(TAG, "onCreate out" + Thread.currentThread().getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand in " + Thread.currentThread().getName());
        if (intent.hasCategory(TAG)) {
            Log.d(TAG, "category found");
            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
            Log.d(TAG, "message sent");
        }
        // If we get killed, after returning from here, restart
        Log.d(TAG, "onStartCommand out " + Thread.currentThread().getName());
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    public static class Action {
        public static final String ADD_PLAYER = "add";
        public static final String REMOVE_PLAYER = "remove";
        public static final String LIST = "list";
        public static final String STOP = "stop discovery";
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private NsdManager mNsdManager;

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            Log.d(TAG, "handleMessage in " + Thread.currentThread().getName());
            if (msg.obj != null) {
                Intent intent = (Intent) msg.obj;
                if (Action.LIST.equals(intent.getAction())) {
                    continueDiscovery = true;
                    if (mNsdManager == null) {
                        Log.d(TAG, "searching NsdManager service");
                        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
                        Log.d(TAG, "NsdManager service found: " + (mNsdManager != null));
                    }
                    if (discovery == null) {
                        Log.d(TAG, "create Discovery object");
                        discovery = getDiscovery();
                        Log.d(TAG, "Discovery object has been created");
                        Log.d(TAG, "start discovery");
                        mNsdManager.discoverServices(NsdHelperStringLiterals.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discovery);
                    }
                } else if (Action.STOP.equals(intent.getAction())) {
                    continueDiscovery = false;
                }
            }

            Log.d(TAG, "handleMessage out " + Thread.currentThread().getName());
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
//            stopSelf(msg.arg1);
        }
    }
}
