package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MyHandler {
    private Handler mHandler;

    public Handler getHandler() {
        return mHandler;
    }

    public MyHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void sendMessage(String tag, String message)
    {
        Message msg = mHandler.obtainMessage();
        Bundle bundle = msg.getData();
        bundle.putString(tag, message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}
