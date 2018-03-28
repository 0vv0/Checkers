package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;

public class MyHandler implements Serializable{
    public static final String TAG = MyHandler.class.getName();
    private Handler mHandler;

    public void sendMessage(Bundle bundle) {
        Log.d(TAG, bundle.toString());
        Message message = mHandler.obtainMessage();
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public MyHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void sendMessage(Type type)
    {
        Log.d(TAG, type.name());
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putString(type.name(), "");
        message.setData(bundle);
        mHandler.sendMessage(message);
    }
    public void sendMessage(String  tag, Type key, Serializable seri){
        Log.d(tag, key + "=\n" + seri);
        Message message = mHandler.obtainMessage();

        Bundle bundle = message.getData();
        bundle.putString(tag, key.name());
        bundle.putSerializable(key.name(), seri);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void sendMessage(Type key, Serializable seri){
        Log.d(TAG, key + "=\n" + seri);
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putSerializable(key.name(), seri);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }


}
