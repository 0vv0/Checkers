package os.checkers.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import os.checkers.model.Checker;
import os.checkers.model.Coordinate;
import os.checkers.model.Square;

class ViewWithChecker extends Button {
    public ViewWithChecker(Context context) {
        super(context);
    }

    private Drawable backgroundColor;
    private Coordinate coordinate;

    public Coordinate getCoordinate() {
        return coordinate;
    }

    View set(final Square square, OnClickListener onClickListener) {
        this.setBackgroundColor(square.isBlack() ? Color.DKGRAY : Color.LTGRAY);
        backgroundColor = this.getBackground();
        Checker checker = square.getChecker();
        if (checker != null) {
            this.setTextColor(checker.isBlack() ? Color.BLACK : Color.WHITE);
//            this.setText(checker.getType() == Checker.Type.King ? "\u0398" : "\u039F");
            this.setText((checker.getType() == Checker.Type.King ? "\u0298" : "\u039F")
//                    + square.getCoord().getRow() + square.getCoord().getColumn()
            );
        }
        if (onClickListener != null) {
            setOnClickListener(onClickListener);
        }
        coordinate = square.getCoord();
        return this;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            this.setBackground(null);
        } else {
            this.setBackground(backgroundColor);
        }
    }
}
