package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.net.InetSocketAddress;

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

    public void sendMessage(HandlerType key, Serializable data){
        Log.d(TAG, key + "=\n" + data);
        Message message = this.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putSerializable(key.name(), data);
        message.setData(bundle);
        message.what = key.ordinal();
        this.sendMessage(message);
    }

    public void updatePosition(Serializable data){
        Log.d(TAG, "position");
        Message message = this.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putSerializable(HandlerType.UPDATE_POSITION.name(), data);
        message.setData(bundle);
        message.what = HandlerType.UPDATE_POSITION.ordinal();
        this.sendMessage(message);
    }

    public void sendError(String errorMessage){
        sendMessage(HandlerType.ERROR, errorMessage);
    }

    public void connectionRequest(InetSocketAddress socketAddress) {
        Log.d(TAG, "from=" + socketAddress);
        Message message = this.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putSerializable(HandlerType.CONNECTION_REQUEST.name(), socketAddress);
        message.setData(bundle);
        message.what = HandlerType.CONNECTION_REQUEST.ordinal();
        this.sendMessage(message);
    }
}