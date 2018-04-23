package os.checkers.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import os.checkers.BuildConfig;
import os.checkers.R;
import os.checkers.model.Color;
import os.checkers.model.Coordinate;
import os.checkers.model.Field;
import os.checkers.network3.Player;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements ViewWithChecker.OnClickListener, Observer {
    private static final String TAG = MainActivity.class.getName();
    private static final String DISCONNECTED = "DIS";

    private TextView localHost;
    private TextView localPort;
    private EditText remoteHost;
    private EditText remotePort;
    private Button local;
    private Button remote;

    private List<ViewWithChecker> selectedSquare = new ArrayList<>();
    private Color player = Color.White;

    private ServiceConnection pConnectionToService;
    private Player pPlayerService;
    private Intent pIntentToService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localHost = (TextView) findViewById(R.id.localhost);
        localPort = (TextView) findViewById(R.id.localport);
        remoteHost = (EditText) findViewById(R.id.remotehost);
        remotePort = (EditText) findViewById(R.id.remoteport);
        local = (Button) findViewById(R.id.local);
        remote = (Button) findViewById(R.id.remote);

        pIntentToService = new Intent(this, Player.class);
        pConnectionToService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected()");
                pPlayerService = ((Player.PlayerBinder) service).getService();
                fillLocalInetAddressPort();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected()");
                pPlayerService = null;
                fillLocalInetAddressPort();
            }
        };

        Log.d(TAG, Thread.currentThread().getName() + " has been started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Field.getInstance().addObserver(this);
        load(null);
        bindService(pIntentToService, pConnectionToService, 0);
    }

    @Override
    protected void onPause() {
        save(null);
        if (pPlayerService != null) {
            unbindService(pConnectionToService);
        }
        Field.getInstance().deleteObserver(this);
        super.onPause();
    }

    private int getSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels < metrics.heightPixels
                ? metrics.widthPixels
                : metrics.heightPixels;
    }

    private void getMainLayout() {
        selectedSquare.clear();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.removeAllViews();
        MainLayout fieldLayout = new MainLayout(linearLayout.getContext(), player);
        linearLayout.addView(fieldLayout);
        fieldLayout.spawn(getSize() / Field.getInstance().size(), Field.getInstance(), this);
    }

    @Override
    public void onClick(final View v) {
        final ViewWithChecker vwc = (ViewWithChecker) v;
        Coordinate coordinate = vwc.getCoordinate();
        if (selectedSquare.size() > 0 &&
                vwc.equals(selectedSquare.get(selectedSquare.size() - 1))) {//remove last selected
            selectedSquare.remove(selectedSquare.size() - 1);
            vwc.setSelected(false);
        } else {
            if (selectedSquare.size() == 0) {
                if (
                        Field.getInstance().isSelectable(coordinate)
                        ) {
                    selectedSquare.add((ViewWithChecker) v);
                    vwc.setSelected(true);
                }
            } else if (!selectedSquare.contains(v) && Field.getInstance().isAllowed(
                    selectedSquare.get(selectedSquare.size() - 1).getCoordinate(), //from
                    vwc.getCoordinate(), //to
                    Field.getInstance().get(selectedSquare.get(0).getCoordinate()).getChecker())) {//checker
                selectedSquare.add(vwc);
                vwc.setSelected(true);
                if (selectedSquare.size() == 2 &&
                        Math.abs(selectedSquare.get(0).getCoordinate().getRow() - selectedSquare.get(1).getCoordinate().getRow()) == 1
                        ) {
                    move(v);
                }
            }
        }
        if (selectedSquare.size() > 1 &&
                !Field.getInstance().hasAllowedMoves(
                        selectedSquare.get(selectedSquare.size() - 1).getCoordinate(),
                        Field.getInstance().get(selectedSquare.get(0).getCoordinate()).getChecker()
                )) {
            move(v);
        }
    }

    public void exit(View view) {
        save(null);
//        connect(view);
        System.exit(0);
    }

    public void restart(View view) {
        Field.getInstance().newGame();
    }

    public void save(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("mPrefs", MODE_PRIVATE).edit();
        editor.putString("board", Field.getInstance().getJson());
        editor.putString("player", player.name());
        editor.apply();
    }

    public void load(View view) {
        SharedPreferences prefs = getSharedPreferences("mPrefs", MODE_PRIVATE);
        if (prefs.contains("board") && prefs.contains("player")) {
            selectedSquare.clear();
            player = Color.valueOf(prefs.getString("player", ""));
            Field.fromJson(prefs.getString("board", ""));
//            getMainLayout();
        }
    }
//
//    public void clearSave(View v) {
//        SharedPreferences.Editor editor = getSharedPreferences("mPrefs", MODE_PRIVATE).edit();
//        editor.clear();
//        editor.apply();
//    }

    public void move(View view) {
        if (selectedSquare.size() > 1) {
            List<Coordinate> coordinates = new ArrayList<>();
            for (int i = 0; i < selectedSquare.size(); i++) {
                coordinates.add(selectedSquare.get(i).getCoordinate());
            }
            selectedSquare.clear();
            Field.getInstance().move(coordinates);

            Toast.makeText(this, "sending position...", Toast.LENGTH_SHORT).show();
            if (remote.isSelected()) {
                sendPosition();
            }
        }
    }

    void sendPosition() {
        Intent intent = new Intent(pIntentToService);
        intent.setAction(Player.IntentAction.UPDATE_POSITION.name());
        intent.putExtra(Player.IntentAction.UPDATE_POSITION.name(), Field.getInstance().getJson());
        sendBroadcast(intent);
    }

    @Override
    public void update(Observable o, Object arg) {
        getMainLayout();
    }

    private void fillLocalInetAddressPort() {
        localHost.setText(getLocalWIFIAddress());
        if (pPlayerService != null) {
            localPort.setText(String.valueOf(pPlayerService.getLocalPort()));
//            if (BuildConfig.DEBUG) {
//                remoteHost.setText("127.0.0.1");
//                remotePort.setText(localPort.getText());
//            }
        } else {
            localPort.setText(DISCONNECTED);
        }
    }

    public void local(View v) {
        Log.d(TAG, "request localhost data");
        if (pPlayerService != null) {

            fillLocalInetAddressPort();
        } else {
            bindService(pIntentToService, pConnectionToService, BIND_AUTO_CREATE);
        }
    }

    public void remote(View v) {
        if (v.isSelected()) {
            v.setSelected(false);
            unbindService(pConnectionToService);
        } else {

            if (remoteHost.getText().toString().isEmpty() || remotePort.getText().toString().isEmpty() || remotePort.getText().toString().equals(DISCONNECTED)) {
                Toast.makeText(this, "HOST or PORT not set", Toast.LENGTH_LONG).show();
                return;
            }
            if (BuildConfig.DEBUG) {
                final Player localEnemy = new Player();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            localEnemy.connectToServer(
                                    InetAddress.getByName(remoteHost.getText().toString()),
                                    Integer.valueOf(remotePort.getText().toString())
                            );
                        } catch (IOException e) {
                            Log.d(TAG, "incorrect InetAddress or Port" + e);
                        } catch (IllegalStateException e) {
                            Log.d(TAG, "sync error" + e);
                        }
                    }
                });


                return;
            }

            if (pPlayerService != null) {
                try {
                    pPlayerService
                            .connectToServer(
                                    InetAddress.getByName(remoteHost.getText().toString()),
                                    Integer.valueOf(remotePort.getText().toString())
                            );
                    v.setSelected(true);
                } catch (IOException e) {
                    Log.d(TAG, "incorrect InetAddress or Port" + e);
                }
            }
        }
    }

    private String getLocalWIFIAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            return null;
        }

        return ipAddressString;
    }

}
