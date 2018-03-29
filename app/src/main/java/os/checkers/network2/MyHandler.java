package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;

public class MyHandler implements Serializable {
    public static final String TAG = MyHandler.class.getName();
    private Handler mHandler;

//    public void sendMessage(Bundle bundle) {
//        Log.d(TAG, bundle.toString());
//        Message message = mHandler.obtainMessage();
//        message.setData(bundle);
//        message.what = HandlerType.values().length;
//        mHandler.sendMessage(message);
//    }

    public Handler getHandler() {
        return mHandler;
    }

    public MyHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void sendMessage(HandlerType type) {
        sendMessage(type, "");
    }

    public void sendMessage(HandlerType key, String msg) {
        Log.d(TAG, key + "=\n" + msg);
        Message message = mHandler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putString(key.name(), msg);
        message.setData(bundle);
        message.what = key.ordinal();
        mHandler.sendMessage(message);
    }



//    public void sendMessage(HandlerType key, Serializable seri){
//        Log.d(TAG, key + "=\n" + seri);
//        Message message = mHandler.obtainMessage();
//        Bundle bundle = message.getData();
//        bundle.putSerializable(key.name(), seri);
//        message.setData(bundle);
//        mHandler.sendMessage(message);
//    }


}
