package os.checkers.ui;

import android.content.Context;
import android.widget.GridLayout;
import os.checkers.model.Color;
import os.checkers.model.CoordinateImpl;
import os.checkers.model.Field;
import os.checkers.model.Square;

/**
 * Created by Oleksii.Sergiienko on 16.11.2017.
 */
public class MainLayout extends GridLayout {
    private Color player;

    public MainLayout(Context context) {
        super(context);
        player = Color.White;
    }

    public MainLayout(Context context, Color player) {
        super(context);
        if (player == null) {
            throw new IllegalArgumentException("White or Black side should be selected!");
        }
        this.player = player;
    }

    public void spawn(int size, Field field, OnClickListener onClickListener) {
        removeAllViews();
        setColumnCount(field.size());
        setRowCount(field.size());
        if (player == Color.White) {
            spawnGridForWhite(size, field, onClickListener);
        } else {
            spawnGridForBlack(size, field, onClickListener);
        }
    }

    private void spawnGridForBlack(int size, Field field, OnClickListener onClickListener) {
        for (int i = 0; i < field.size(); i++) {
            for (int j = field.size() - 1; j >= 0; j--) {
//                CoordinateImpl coordinate = CoordinateImpl.get(i, j);
//                ViewWithChecker vwc = new ViewWithChecker(getContext());
//                vwc.set(field.get(coordinate), field.get(coordinate).isBlack() ? onClickListener : null);
//                addView(vwc, i * field.size() + (field.size() - 1 - j));
//                vwc.getLayoutParams().width = size;
//                vwc.getLayoutParams().height = size;
                addView(field.get(CoordinateImpl.get(i, j)), i * field.size() + (field.size() - 1 - j), size, onClickListener);
            }
        }
    }

    private void spawnGridForWhite(int size, Field field, OnClickListener onClickListener) {
        for (int i = field.size() - 1; i >= 0; i--) {
            for (int j = 0; j < field.size(); j++) {
//                CoordinateImpl coordinate = CoordinateImpl.get(i, j);
//                ViewWithChecker vwc = new ViewWithChecker(getContext());
//                vwc.set(field.get(coordinate), field.get(coordinate).isBlack() ? onClickListener : null);
//                addView(vwc, (field.size() - 1 - i) * field.size() + j);
//                vwc.getLayoutParams().width = size;
//                vwc.getLayoutParams().height = size;
                addView(field.get(CoordinateImpl.get(i, j)), (field.size() - 1 - i) * field.size() + j, size, onClickListener);
            }
        }
    }

    public MainLayout rotate() {
        player = player.getNext();
        return this;
    }

    private void addView(final Square square, int index, int size, OnClickListener onClickListener) {
        ViewWithChecker vwc = new ViewWithChecker(getContext());
        vwc.set(square, square.isBlack() ? onClickListener : null);
        addView(vwc, index);
        vwc.getLayoutParams().width = size;
        vwc.getLayoutParams().height = size;
    }
}
