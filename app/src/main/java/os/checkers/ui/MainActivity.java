package os.checkers.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
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
import os.checkers.network.NsdService;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements ViewWithChecker.OnClickListener, Observer {
    private static final String TAG = MainActivity.class.getName();

    public enum Intents {
        PLAYERS_LIST,
        DISCONNECTED,
        SET_POSITION;

        static boolean containsName(String name) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    private TextView mTextView;

    //    private Field field;
    private List<ViewWithChecker> selectedSquare = new ArrayList<>();
    private Color player = Color.White;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Intents.valueOf(intent.getAction())) {
                case DISCONNECTED:
                    Toast
                            .makeText(getApplicationContext(), "Disconnected...", Toast.LENGTH_SHORT)
                            .show();
                    mTextView.setText("Disconnected...");
                    break;
                case PLAYERS_LIST:
                    Toast
                            .makeText(getApplicationContext(), "Player list changed...", Toast.LENGTH_SHORT)
                            .show();
                    mTextView.setText(intent.getBundleExtra("list").toString());
                    break;
                case SET_POSITION:
                    Toast
                            .makeText(getApplicationContext(), "Position changed...", Toast.LENGTH_LONG)
                            .show();
                    if (intent.getStringExtra(NsdService.POSITION) != null) {
                        mTextView.setText(intent.getStringExtra(NsdService.POSITION));
                        Field.fromJson(intent.getStringExtra(NsdService.POSITION));
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intents.PLAYERS_LIST.name());
        intentFilter.addAction(Intents.SET_POSITION.name());
        intentFilter.addAction(Intents.DISCONNECTED.name());
        registerReceiver(receiver, intentFilter);

        Field.getInstance().addObserver(this);
        load(null);
    }

    @Override
    protected void onPause() {
        save(null);
        Field.getInstance().deleteObserver(this);
        unregisterReceiver(receiver);
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
        }
    }

    public void list(View v) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), NsdService.class);
        intent.setAction(IntentActions.REQUEST_PLAYERS_LIST.name());

        Toast.makeText(this, "searching for players...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, intent.getAction());
        this.startService(intent);
    }

    @Override
    public void update(Observable o, Object arg) {
        getMainLayout();
    }
}
