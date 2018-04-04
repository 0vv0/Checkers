package os.checkers.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import os.checkers.R;
import os.checkers.model.Color;
import os.checkers.model.Coordinate;
import os.checkers.model.Field;
import os.checkers.network2.HandlerType;
import os.checkers.network2.MyHandler;
import os.checkers.network2.Server;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements ViewWithChecker.OnClickListener, Observer, Handler.Callback {
    private static final String TAG = MainActivity.class.getName();

    private TextView localHost;
    private TextView localPort;
    private EditText remoteHost;
    private EditText remotePort;
    private Button local;
    private Button remote;

    private Server server;

    private List<ViewWithChecker> selectedSquare = new ArrayList<>();
    private Color player = Color.White;

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
        Log.d(TAG, Thread.currentThread().getName() + " has been started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Field.getInstance().addObserver(this);
        MyHandler localHandler = new MyHandler(this);
        Log.d(TAG, "localHandler.looper is " + localHandler.getLooper());

        load(null);
    }

    @Override
    protected void onPause() {
        save(null);

        if (server != null) {
            if (server.isAlive()) {
                server.interrupt();
            }
            server = null;
        }
        local.setSelected(false);

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
            if (local.isSelected()) {

            }
        }
    }


    public void connect(View v) {
        if (v.isSelected()) {
            server.interrupt();
            server = null;
            v.setSelected(false);

        } else {
            if (remoteHost.getText().toString().isEmpty() || remotePort.getText().toString().isEmpty()) {
                Toast.makeText(this, "HOST or PORT not set", Toast.LENGTH_LONG).show();
                return;
            }

            v.setSelected(true);
        }
    }

    void sendPosition() {

    }

    @Override
    public void update(Observable o, Object arg) {
        getMainLayout();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what < 0 || msg.what > HandlerType.values().length - 1) {
            return false;
        }
        HandlerType type = HandlerType.values()[msg.what];
        Serializable data = msg.getData().getSerializable(type.name());

        switch (type) {
            case NO_PLAYER:
                local.setSelected(false);
                break;
            case SENT:
                Toast.makeText(this, "Position has been sent", Toast.LENGTH_LONG).show();
                break;
            case ERROR:
                Toast.makeText(this, (String)data, Toast.LENGTH_LONG).show();
                break;
            case UPDATE_POSITION:
                Field.fromJson((String)data);
                break;
        }

        return false;
    }

    public void local(View v) {
        Log.d(TAG, "request localhost data");

        getLocalWIFIAddress();
    }

    public void getLocalWIFIAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        Log.d(TAG, "" + wifiInfo);
        InetAddress inetAddress = null;

    }

}
