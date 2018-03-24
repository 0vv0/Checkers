package os.checkers.network;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


public class Discovery implements NsdManager.DiscoveryListener, NsdManager.ResolveListener {
    public static final String TAG = Discovery.class.getName();
    private static volatile Discovery instance;
    private final DiscoveryResolver mResolver;

    private Discovery(final DiscoveryResolver resolver) {
        mResolver = resolver;
    }

    public static Discovery getInstance(final DiscoveryResolver resolver) {
        if (instance == null) {
            synchronized (Discovery.class) {
                instance = new Discovery(resolver);
            }
        }
        return instance;
    }

    @Override
    public void onStartDiscoveryFailed(String nsdService, int i) {
        Log.d(TAG, "Start discover for " + nsdService + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_START_FAILED, mHandler);
    }

    @Override
    public void onStopDiscoveryFailed(String nsdService, int i) {
        Log.d(TAG, "Start discover for " + nsdService + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_STOP_FAILED, mHandler);
    }

    @Override
    public void onDiscoveryStarted(String nsdService) {
        Log.d(TAG, "Started discovery: " + nsdService);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_STARTED, mHandler);
    }

    @Override
    public void onDiscoveryStopped(String nsdService) {
        Log.d(TAG, "Discovery stopped: " + nsdService);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_STOPPED, mHandler);
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        Log.d(TAG, "Found service: " + nsdServiceInfo.getServiceName());
        if (!nsdServiceInfo.getServiceType().equals(NsdHelperStringLiterals.SERVICE_TYPE)) {
            Log.d(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
//        } else if (nsdServiceInfo.getServiceName().equals(mNsdServiceName)) {
//            Log.d(TAG, "Same machine: " + nsdServiceInfo);
        } else if (nsdServiceInfo.getServiceName().contains(NsdHelperStringLiterals.DEFAULT_SERVICE_NAME)) {
            onServiceResolved(nsdServiceInfo);
//            NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_SERVICE_FOUND, mHandler);
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        mResolver.onLost(serviceInfo);
    }

    @Override
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.d(TAG, "Resolve for " + nsdServiceInfo.getServiceName() + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.RESOLVE_FAILED, mHandler);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        mResolver.onResolved(serviceInfo);
    }

}
