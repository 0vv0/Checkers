package os.checkers.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NsdService extends IntentService {
    private enum IntentsForRegistrationListener {
        SERVICE_REGISTERED,
        SERVICE_UNREGISTERED,
        SERVICE_REGISTRATION_FAILED,
        SERVICE_UNREGISTRATION_FAILED;

        static boolean containsName(String name) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    private enum IntentsForDiscoveryListener {
        StartDiscoveryFailed,
        StopDiscoveryFailed,
        DiscoveryStarted,
        DiscoveryStopped,
        ServiceFound,
        ServiceLost;

        static boolean containsName(String name) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    private enum IntentsForResolveListener {
        ResolveFailed,
        ServiceResolved;

        static boolean containsName(String name) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final static String SERVICETYPE = "_http._tcp";
    private final static String TAG = NsdService.class.getName();
    private String SERVICENAME = NsdService.class.getName();

    public NsdService() {
        this(NsdService.class.getName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NsdService(String name) {
        super(name);
    }

    /**
     * CopyOnWriteArrayList is synchronized
     */
    private final List<NsdServiceInfo> services = new CopyOnWriteArrayList<>();

    @Override
    protected void onHandleIntent(final Intent intent) {
        Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_LONG).show();
        if (IntentsForRegistrationListener.containsName(intent.getAction())) {
            onHandleRegistration(intent);
        } else if (IntentsForResolveListener.containsName(intent.getAction())){
            onHandleResolve(intent);
        } else if (IntentsForDiscoveryListener.containsName(intent.getAction())){
            onHandleDiscovery(intent);
        }else {
            onHandleIntentActions(intent);
        }
    }

    private void onHandleRegistration(final Intent intent) {

    }
    private void onHandleResolve(final Intent intent) {

    }
    private void onHandleDiscovery(final Intent intent) {

    }
    private void onHandleIntentActions(final Intent intent) {
        switch (IntentActions.valueOf(intent.getAction())) {
            case NSDSERVICE_REQUEST_PLAYERS_LIST:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentActions.LIST_PLAYERS.name());
                broadcastIntent.putStringArrayListExtra("list", getIds(services));
                sendBroadcast(broadcastIntent);
                break;
        }
    }

    private NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, getErrorString(serviceInfo, errorCode));
            sendToMySelf(IntentsForRegistrationListener.SERVICE_REGISTRATION_FAILED.name());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, getErrorString(serviceInfo, errorCode));
            sendToMySelf(IntentsForRegistrationListener.SERVICE_UNREGISTRATION_FAILED.name());
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, serviceInfo.toString());
            sendToMySelf(IntentsForRegistrationListener.SERVICE_REGISTERED.name());
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, serviceInfo.toString());
            sendToMySelf(IntentsForRegistrationListener.SERVICE_UNREGISTERED.name());
        }
    };
    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Start discovery failed with code: " + errorCode + " - " + serviceType);
            sendToMySelf(IntentsForDiscoveryListener.StartDiscoveryFailed.name());
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Stop discovery failed with code: " + errorCode + " - " + serviceType);
            sendToMySelf(IntentsForDiscoveryListener.StopDiscoveryFailed.name());
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.e(TAG, "Discovery started: " + serviceType);
            sendToMySelf(IntentsForDiscoveryListener.DiscoveryStarted.name());
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.e(TAG, "Discovery stoped: " + serviceType);
            sendToMySelf(IntentsForDiscoveryListener.DiscoveryStopped.name());
        }

        @Override
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            Log.i(TAG, nsdServiceInfo.toString());
            sendToMySelf(IntentsForDiscoveryListener.ServiceFound.name());
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.i(TAG, serviceInfo.toString());
            sendToMySelf(IntentsForDiscoveryListener.ServiceLost.name());
        }
    };
    private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, getErrorString(serviceInfo, errorCode));
            sendToMySelf(IntentsForResolveListener.ResolveFailed.name());
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.e(TAG, serviceInfo.toString());
            sendToMySelf(IntentsForResolveListener.ServiceResolved.name());
        };
    };

    private NsdServiceInfo getNsdServiceInfo() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICENAME);
        serviceInfo.setServiceType(SERVICETYPE);
        try {
            serviceInfo.setHost(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            String message = "Cann't find IP address\n" + e.getLocalizedMessage();
            Log.e(TAG, message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        return serviceInfo;
    }

    private static ArrayList<String> getIds(List<NsdServiceInfo> services) {
        final ArrayList<String> list = new ArrayList<>();
        for (NsdServiceInfo service : services) {
            list.add(service.getHost().toString());
        }
        return list;
    }

    private static String getErrorString(NsdServiceInfo serviceInfo, int errorCode) {
        return String.valueOf(errorCode) + "\n" + serviceInfo.toString();
    }

    private void sendToMySelf(String action) {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(action);
        startService(intent);
    }

}
