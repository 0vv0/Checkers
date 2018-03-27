package os.checkers.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import os.checkers.R;
import os.checkers.model.Color;
import os.checkers.model.Coordinate;
import os.checkers.model.Field;
import os.checkers.network.IntentActions;
import os.checkers.network.PlayerService;
import os.checkers.network2.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements ViewWithChecker.OnClickListener, Observer, Handler.Callback {
    private static final String TAG = MainActivity.class.getName();

    private TextView mTextView;
    private List<String> mPlayers = new ArrayList<>();
    private Server server;

    //    private Field field;
    private List<ViewWithChecker> selectedSquare = new ArrayList<>();
    private Color player = Color.White;
//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            switch (IntentActions.valueOf(intent.getAction())) {
////                case LIST_PLAYERS:
////                    Toast
////                            .makeText(getApplicationContext(), "Player list changed...", Toast.LENGTH_SHORT)
////                            .show();
////                    mTextView.setText(intent.getStringExtra(NsdService.PLAYERS));
////                    break;
////                case SET_POSITION:
////                    Toast
////                            .makeText(getApplicationContext(), "Position changed...", Toast.LENGTH_SHORT)
////                            .show();
////                    if (intent.getStringExtra(NsdService.POSITION) != null) {
////                        mTextView.setText(intent.getStringExtra(NsdService.POSITION));
////                        Field.fromJson(intent.getStringExtra(NsdService.POSITION));
////                    }
////                    break;
////            }
//            if (intent.hasCategory(PlayerService.TAG)) {
//                if (intent.hasExtra(PlayerService.Action.ADD_PLAYER)) {
//                    addPlayer(intent.getParcelableExtra(PlayerService.Action.ADD_PLAYER).toString());
//                }
//                if (intent.hasExtra(PlayerService.Action.REMOVE_PLAYER)) {
//                    removePlayer(intent.getParcelableExtra(PlayerService.Action.REMOVE_PLAYER).toString());
//                }
//                update(mTextView, mPlayers);
//            }
//        }
//    };

    private void addPlayer(String player) {
        mPlayers.add(player.intern());
    }

    private void removePlayer(String player) {
        mPlayers.remove(player.intern());
    }

    private void update(final TextView view, List<String> players) {
        StringBuilder p = new StringBuilder();
        for (String player : players) {
            p.append(player);
        }
        view.setText(p);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.info);
        Log.d(TAG, Thread.currentThread().getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(server==null||!server.isAlive()){
            server = new Server(new Handler(this));
            server.start();
        }
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addCategory(PlayerService.TAG);
//        registerReceiver(receiver, intentFilter);

        Field.getInstance().addObserver(this);
        load(null);
    }

    @Override
    protected void onPause() {
        save(null);
        Field.getInstance().deleteObserver(this);
        if(server!=null&&server.isAlive()){
            server.interrupt();
        }
//        unregisterReceiver(receiver);
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
        connect(view);
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

            Intent intent = new Intent(IntentActions.SEND);
            intent.setPackage(this.getPackageName());
            Toast.makeText(this, "sending position...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, intent.getAction());
            this.startService(intent);
        }
    }

    public void list(View v) {
        Toast.makeText(this, "searching for players...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "searching...");
        startService(PlayerService.Action.LIST, PlayerService.TAG, PlayerService.class);
    }

    public void connect(View v) {
        Toast.makeText(this, "stopping search ...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "stopping search...");
        mPlayers.clear();
        startService(PlayerService.Action.STOP, PlayerService.TAG, PlayerService.class);
    }

    private void startService(String action, String category, Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        intent.addCategory(category);
        startService(intent);
    }

    @Override
    public void update(Observable o, Object arg) {
        getMainLayout();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
