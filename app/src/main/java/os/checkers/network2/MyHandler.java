package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;

public class MyHandler extends Handler implements Serializable {
    public static final String TAG = MyHandler.class.getName();
//    private Handler mHandler;

    public MyHandler() {
        super();
    }

    public MyHandler(Callback callback) {
        super(callback);
    }

    public MyHandler(Looper looper) {
        super(looper);
    }

    public MyHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    public void sendMessage(HandlerType type) {
        sendMessage(type, "");
    }

    public void sendMessage(HandlerType key, String msg) {
        Log.d(TAG, key + "=\n" + msg);
        Message message = this.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putString(key.name(), msg);
        message.setData(bundle);
        message.what = key.ordinal();
        this.sendMessage(message);
    }
}