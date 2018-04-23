package os.checkers.mvp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import os.checkers.R;

public class MVPActivity extends Activity implements MVP.View, View.OnClickListener {

    private MVP.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mvp);
        presenter = new Presenter(this, this);
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

//    @Override
//    public void show(View layout) {
//        getFieldContainer().removeAllViews();
//        if (layout != null) {
//            getFieldContainer().addView(layout);
//        }
//    }

    @Override
    public void show(MVP.Presenter.State[][] state) {
        getFieldContainer().removeAllViews();
        getFieldContainer().addView(generateRootGridLayout(state));
    }

    private GridLayout generateRootGridLayout(MVP.Presenter.State[][] state) {
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setRowCount(state.length);
        gridLayout.setColumnCount(state.length);

        for (int row = 0; row < state.length; row++) {
            for (int column = 0; column < state.length; column++) {
                TextView view = new ViewCell(this, row, column);
                if (!state[row][column].isWhite()) {
                    view.setBackgroundColor(android.graphics.Color.DKGRAY);
//                    view.setOnClickListener(this);
                    if (state[row][column].hasWhite() != null) {
                        view.setTextColor(state[row][column].hasWhite()
                                ? Color.WHITE
                                : Color.BLACK);
                        view.setText(state[row][column].hasWhiteKing() == null
                                ? getString(R.string.checker)
                                : getString(R.string.king));
                    }
                } else {
                    view.setBackgroundColor(android.graphics.Color.LTGRAY);
                }
                view.setOnClickListener(this);
                gridLayout.addView(view, convertToIndex(row, column, state.length));
            }
        }

        return gridLayout;
    }

    private int convertToIndex(int row, int column, int rowLength){
//        Toast.makeText(this, "[" + row + ", " + column + "]=" + rowLength, Toast.LENGTH_SHORT).show();
        return row*rowLength+column;
    }

    @Override
    public ViewGroup getFieldContainer() {
        return (ViewGroup) findViewById(R.id.root);
    }

    public void restart(View v) {
        presenter.newGame();
    }

    public void move(View v) {
        presenter.move();
    }

    public void exit(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exit Application?");
        alertDialogBuilder
                .setMessage("Click yes to exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
//        Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
        if (v instanceof ViewCell) {
                ViewCell viewCell = (ViewCell)v;
                presenter.click(viewCell.row, viewCell.column);
        }
    }

    static class ViewCell extends Button {
        ViewCell(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.row = attrs.getAttributeIntValue(0, -1);
            this.column = attrs.getAttributeIntValue(1, -1);
        }

        ViewCell(Context context, int row, int column) {
            super(context);
            this.row = row;
            this.column = column;
        }

        final int row;
        final int column;
    }
}
