package os.checkers.network;

import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public abstract class Discovery implements NsdManager.DiscoveryListener, NsdManager.ResolveListener {
    final String TAG = Discovery.class.getName();

    @Override
    public void onStartDiscoveryFailed(String nsdService, int i) {
        Log.d(TAG, "Start discovery for " + (nsdService != null ? nsdService : "null") + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_START_FAILED, mHandler);
    }

    @Override
    public void onStopDiscoveryFailed(String nsdService, int i) {
        Log.d(TAG, "Stop discovery for " + (nsdService != null ? nsdService : "null") + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_STOP_FAILED, mHandler);
    }

    @Override
    public void onDiscoveryStarted(String nsdService) {
        Log.d(TAG, "Started discovery: " + (nsdService != null ? nsdService : "null"));
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.DISCOVERY_STARTED, mHandler);
    }

    @Override
    public void onDiscoveryStopped(String nsdService) {
        Log.d(TAG, "Discovery stopped: " + (nsdService != null ? nsdService : "null"));
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
    public void onResolveFailed(NsdServiceInfo nsdService, int i) {
        Log.d(TAG, "Resolve for " + (nsdService != null ? nsdService : "null") + " failed: " + i);
//        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.RESOLVE_FAILED, mHandler);
    }

//    @Override
//    public void onServiceLost(NsdServiceInfo serviceInfo) {
//        Log.d(TAG, "Lost the " + serviceInfo);
//        Intent intent = new Intent(NsdForCheckers.Action.REMOVE_PLAYER);
//        intent.addCategory(TAG);
//        intent.putExtra(NsdForCheckers.Action.REMOVE_PLAYER, serviceInfo);
//        sendBroadcast(intent);
//    }
//
//    @Override
//    public void onServiceResolved(NsdServiceInfo serviceInfo) {
//        Log.d(TAG, "Resolved to " + serviceInfo);
//        Intent intent = new Intent(NsdForCheckers.Action.ADD_PLAYER);
//        intent.addCategory(TAG);
//        intent.putExtra(NsdForCheckers.Action.ADD_PLAYER, serviceInfo);
//        sendBroadcast(intent);
//    }
}
