package os.checkers.network;

import android.net.nsd.NsdServiceInfo;

public interface DiscoveryResolver {
    void onResolved(NsdServiceInfo serviceInfo);

    void onLost(NsdServiceInfo serviceInfo);
}
