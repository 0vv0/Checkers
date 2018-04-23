package os.checkers.mvp;

import android.view.ViewGroup;

public interface MVP {
    interface View {
        void show(Presenter.State[][] state);

        ViewGroup getFieldContainer();
    }

    interface Presenter{
        void onPause();

        void onResume();

        void newGame();

        void move();

        void click(int row, int column);

        interface State {
            boolean isWhite();
            Boolean hasWhite();
            Boolean hasWhiteKing();
        }
    }


}
