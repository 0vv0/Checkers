package os.checkers.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import os.checkers.R;
import os.checkers.model.Color;
import os.checkers.model.Coordinate;
import os.checkers.model.Field;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ViewWithChecker.OnClickListener {
    private Field field;
    private List<ViewWithChecker> selectedSquare = new ArrayList<>();
    private Color player;
//    private int mainLayoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getMainLayout();
    }

    private int getSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels < metrics.heightPixels
                ? metrics.widthPixels
                : metrics.heightPixels;
    }

    private void getMainLayout() {
        if (field == null) {
            field = new Field();
            player = Color.White;

        }
        selectedSquare.clear();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.removeAllViews();
        MainLayout fieldLayout = new MainLayout(linearLayout.getContext(), player);
//        fieldLayout.setId(fieldLayout.hashCode());
//        mainLayoutId = fieldLayout.getId();
        linearLayout.addView(fieldLayout);
        assert field!=null;
        fieldLayout.spawn(getSize() / field.length(), field, this);
    }

    @Override
    public void onClick(final View v) {
        final ViewWithChecker vwc = (ViewWithChecker)v;
        Coordinate coordinate = vwc.getCoordinate();
//        TextView tv = (TextView) findViewById(R.id.messageBox);
//        tv.setText(field.getAllowedMovesFor(coordinate).toString());
        if (selectedSquare.size() > 0 &&
                vwc.equals(selectedSquare.get(selectedSquare.size() - 1))) {//remove last selected
            selectedSquare.remove(selectedSquare.size() - 1);
            vwc.setSelected(false);
        } else {
            if (selectedSquare.size() == 0) {
                if (
                        field.isSelectable(coordinate)
//                        field.get(coordinate).getChecker() != null//only squares with checkers
//                                && field.get(coordinate).getChecker().getColor() == field.getPlayer()//only checkers of our color
                        ) {
                    selectedSquare.add((ViewWithChecker)v);
                    vwc.setSelected(true);
                }
            } else if (field.isAllowed(
                    selectedSquare.get(selectedSquare.size() - 1).getCoordinate(), //from
                    vwc.getCoordinate(), //to
                    field.get(selectedSquare.get(0).getCoordinate()).getChecker())) {//checker
                selectedSquare.add(vwc);
                vwc.setSelected(true);
            }
        }


//        if (selectedSquare == null) {
//            if (field.get(coordinate).isBlack()//only black squares
//                    && field.get(coordinate).getChecker() != null//only squares with checkers
//                    && field.get(coordinate).getChecker().getColor() == field.getPlayer())//only checkers of our color
//            {
//                selectedSquare = v;
//                v.setSelected(true);
//            }
//        } else {
//            if (selectedSquare.equals(v)) {
//                selectedSquare = null;
//                v.setSelected(false);
//            } else {
//                if (field.move(Coordinate.get(selectedSquare.getId()), Coordinate.get(v.getId()))) {
//                    selectedSquare.setSelected(false);
//                    selectedSquare = null;
//                    MainLayout mainLayout = ((MainLayout) findViewById(mainLayoutId));
//                    mainLayout.spawn(getSize() / field.length(), field, this);
//                } else {
//
//                }
//            }
//        }

    }

    public void exit(View view) {
        field = null;
        selectedSquare = null;
        System.exit(0);
    }

    public void restart(View view) {
        field = null;
        getMainLayout();
//        ((MainLayout) findViewById(mainLayoutId)).spawn(getSize() / field.length(), field, this);
    }

    public void save(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("mPrefs", MODE_PRIVATE).edit();
        editor.putString("board", field.getJson());
        editor.putString("player", player.name());
        editor.apply();
    }

    public void load(View view) {
        SharedPreferences prefs = getSharedPreferences("mPrefs", MODE_PRIVATE);
        if (prefs.contains("board") && prefs.contains("player")) {
            selectedSquare.clear();
            field = Field.fromJson(prefs.getString("board", ""));
            player = Color.valueOf(prefs.getString("player", ""));
            getMainLayout();
//            ((MainLayout) findViewById(mainLayoutId)).spawn(getSize() / field.length(), field, this);
        }
    }

    public void clearSave(View v){
        SharedPreferences.Editor editor = getSharedPreferences("mPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public void move(View view) {
        if (selectedSquare.size() > 1) {
            List<Coordinate> coordinates = new ArrayList<>();
            for (int i = 0; i < selectedSquare.size(); i++) {
                coordinates.add(selectedSquare.get(i).getCoordinate());
            }
            selectedSquare.clear();
            field.move(coordinates);
            getMainLayout();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
