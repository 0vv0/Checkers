package os.checkers.mvp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import os.checkers.model.Color;
import os.checkers.model.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static android.content.Context.MODE_PRIVATE;

class Presenter implements MVP.Presenter, Observer{
    @SuppressWarnings("WeakerAccess")
    static final String AUTOSAVE = "_Autosave";
    @SuppressWarnings("WeakerAccess")
    static final String DEFAULT_SAVE_NAME = "mPrefs";
    @SuppressWarnings("WeakerAccess")
    static final String FIELD_PREFIX = "board";
    @SuppressWarnings("WeakerAccess")
    static final String PLAYER_PREFIX = "player";

    private final MVP.View view;
    private final Context context;

    private final Field gameField = Field.getInstance();
    private Color player;
    private final List<State> selected = new ArrayList<>(13);// 12 checkers+1 cell

    Presenter(Context context, MVP.View view) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void onPause() {
        saveGame(AUTOSAVE);
        gameField.deleteObserver(this);
    }

    @Override
    public void onResume() {
        gameField.addObserver(this);
        loadGame(AUTOSAVE);
    }

    @Override
    public void newGame() {
        Toast.makeText(context, "new", Toast.LENGTH_LONG).show();
    }

    @Override
    public void move() {
        Toast.makeText(context, "move", Toast.LENGTH_LONG).show();

    }

    @Override
    public void click(int row, int column) {
        Toast.makeText(context, "[" + row + ", " + column + "]", Toast.LENGTH_SHORT).show();


    }

    private void saveGame(String name) {
        if(player!=null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(DEFAULT_SAVE_NAME, MODE_PRIVATE).edit();
            editor.putString(FIELD_PREFIX + name, gameField.getJson());
            editor.putString(PLAYER_PREFIX + name, player.name());
            editor.apply();
        }
    }

    private void loadGame(String name) {
        SharedPreferences prefs = context.getSharedPreferences(DEFAULT_SAVE_NAME, MODE_PRIVATE);
        if (prefs.contains(FIELD_PREFIX + name) && prefs.contains(PLAYER_PREFIX + name)) {
            player = Color.valueOf(prefs.getString(FIELD_PREFIX + name, ""));
            Field.fromJson(prefs.getString(PLAYER_PREFIX + name, ""));
        } else {
            update(null, null);
        }
    }

    private void clearSave(String name) {
        SharedPreferences.Editor editor = context.getSharedPreferences(DEFAULT_SAVE_NAME, MODE_PRIVATE).edit();
        editor.remove(FIELD_PREFIX + name);
        editor.remove(PLAYER_PREFIX + name);
        editor.apply();
    }

    private void clearAllSaves() {
        SharedPreferences.Editor editor = context.getSharedPreferences(DEFAULT_SAVE_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void update(Observable o, Object arg) {
        view.show(getConvertedField(gameField, selected));
    }

    private static State[][] getConvertedField(final Field field, final List<State> selected){
        State[][] state = new State[field.size()][field.size()];
        for (int i = 0; i <field.size() ; i++) {
            for (int j = 0; j < field.size() ; j++) {
                final int row = i;
                final int column = j;
                state[i][j] = new State() {
                    @Override
                    public boolean isSelected() {
                        return selected!=null&&selected.contains(this);
                    }

                    @Override
                    public boolean isWhite() {
                        return field.get(row, column).isWhite();
                    }

                    @Override
                    public Boolean hasWhite() {
                        return field.get(row, column).isEmpty()?null:field.get(row, column).getChecker().isWhite();
                    }

                    @Override
                    public Boolean hasWhiteKing() {
                        return field.get(row, column).isEmpty()||!field.get(row,column).getChecker().isKing()
                                ?null
                                :field.get(row, column).getChecker().isWhite();
                    }
                };
            }
        }
        return state;
    }
}
