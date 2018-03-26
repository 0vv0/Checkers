package os.checkers.network2;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client extends Thread{
    private static final String TAG = Client.class.getName();
    private final Handler mHandler;
    private Socket mSocket = null;

    public Client(Socket socket, Handler handler) {
        assert socket!=null;assert handler!=null;
        this.mSocket = socket;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        Log.d(TAG, "Started in Thread:" + Thread.currentThread().getName() );

    }


    private void sendMessage(String message){
        Log.d(TAG, "Try to send message:\n" + message);
        new MyHandler(mHandler).sendMessage(TAG, message);
    }

    class ReceivingThread implements Runnable {
        @Override
        public void run() {
            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    String messageStr = null;
                    messageStr = input.readLine();
                    if (messageStr != null) {
                        Log.d(TAG, "Read from the stream: " + messageStr);

                    } else {
                        Log.d(TAG, "The nulls! The nulls!");
                        break;
                    }
                }
                input.close();
            } catch (IOException e) {
                Log.e(TAG, "Server loop error: ", e);
            }
        }
    }
}