package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

public class MyHandler {
    private Handler mHandler;

    public Handler getHandler() {
        return mHandler;
    }

    public MyHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void sendMessage(String tag, String msg)
    {
        Log.d(tag, msg);
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putString(tag, msg);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void sendMessage(String tag, String key, Parcelable obj){
        Log.d(tag, key + "=\n" + obj);
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putParcelable(key, obj);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void sendMessage(String tag, String key, Serializable obj){
        Log.d(tag, key + "=\n" + obj);
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putSerializable(key, obj);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }
}
