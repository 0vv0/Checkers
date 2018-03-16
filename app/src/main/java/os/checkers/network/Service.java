package os.checkers.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.*;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.p2p.WifiP2pManager.*;

public class Service extends IntentService {
    public Service() {
        this(Service.class.getName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Service(String name) {
        super(name);
        peerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());

                if (!refreshedPeers.equals(peers)) {
                    peers.clear();
                    peers.addAll(refreshedPeers);

                    // Perform any other updates needed based on the new list of
                    // peers connected to the Wi-Fi P2P network.
                }

                final Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentActions.LIST_PLAYERS.name());
                ArrayList<String> devices = new ArrayList<>();
                for (WifiP2pDevice peer : peers) {
                    devices.add(peer.toString());
                }
                broadcastIntent.putStringArrayListExtra("devices", devices);
                sendBroadcast(broadcastIntent);
            }
        };
    }

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private PeerListListener peerListListener;

    @Override
    protected void onHandleIntent(final Intent intent) {
        final Intent broadcastIntent = new Intent();


        switch (IntentActions.valueOf(intent.getAction())) {
            case REQUEST_PLAYERS_LIST:
                final WifiP2pManager mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
                final WifiP2pManager.Channel mChannel = mManager.initialize(this, getMainLooper(), null);
                mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mManager.requestPeers(mChannel, peerListListener);
                    }

                    @Override
                    public void onFailure(int reason) {
                        String err = "";
                        switch (reason) {
                            case P2P_UNSUPPORTED:
                                err = "Unsupported";
                                break;
                            case ERROR:
                                err = "Error";
                                break;
                            case BUSY:
                                err = "Busy";
                                break;
                            default:
                                err = String.valueOf(reason);
                                break;
                        }
                        broadcastIntent.setAction(IntentActions.WIFI_ERROR.name());
                        broadcastIntent.putExtra("error", err);
                        sendBroadcast(broadcastIntent);
                    }
                });
                break;

        }
    }

}
