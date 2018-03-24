package os.checkers.network;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;

public class RegistrationListener implements NsdManager.RegistrationListener {
    public static final String TAG = RegistrationListener.class.getName();
    private Handler mHandler;
    private String mServiceName = NsdHelperStringLiterals.DEFAULT_SERVICE_NAME;

    public RegistrationListener(NsdManager nsdManager, Handler handler, int port) {
        assert nsdManager != null;
        assert handler != null;
        mHandler = handler;
        nsdManager
                .registerService(getNsdServiceInfo(port), NsdManager.PROTOCOL_DNS_SD, this);
    }

    public String getServiceName() {
        return mServiceName;
    }

    private NsdServiceInfo getNsdServiceInfo(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(NsdHelperStringLiterals.SERVICE_TYPE);
        return serviceInfo;
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.d(TAG, "Service" + nsdServiceInfo.getServiceName() + " registration failed: " + i);
        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.REGISTRATION_FAILED, mHandler);
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
        Log.d(TAG, "Service" + nsdServiceInfo.getServiceName() + " unregistration failed: " + i);
        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.UNREGISTRATION_FAILED, mHandler);
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
        mServiceName = nsdServiceInfo.getServiceName();
        Log.d(TAG, "Service registered: " + mServiceName);
        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.REGISTERED, mHandler);
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
        Log.d(TAG, "Service unregistered: " + nsdServiceInfo.getServiceName());
        if (nsdServiceInfo.getServiceName().equals(mServiceName)) {
            mServiceName = NsdHelperStringLiterals.DEFAULT_SERVICE_NAME;
        }
        NsdHelper.sendMessage(TAG, NsdHelperStringLiterals.UNREGISTERED, mHandler);
    }
}
