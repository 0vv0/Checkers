package os.checkers.network2;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    public static final String TAG = Server.class.getName();
    private ServerSocket mServerSocket;
    private int mPort;
    private Handler mHandler;
    private Thread mClient;

    public Server(Handler handler) {
        this(0, handler);
    }

    public Server(int port, Handler handler) {
        this.mPort = port;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, "Try to listen on port: " + mPort);
            mServerSocket = new ServerSocket(mPort);
            mPort = mServerSocket.getLocalPort();
            Log.d(TAG, "Will listen on port: " + mPort);
            while (!Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "ServerSocket Created, awaiting connection");
                Socket socket = mServerSocket.accept();
                String remotePlayer = socket.getInetAddress() + ":" + socket.getPort();
                sendMessage(remotePlayer);
                Log.d(TAG, "Connect requested from: " + remotePlayer);
                if (mClient == null||!mClient.isAlive()) {//||mClient.isInterrupted()
                    mClient = new Client(socket, mHandler);
                    mClient.start();
                } else {
                    Log.d(TAG, "Some Client already bound. Ignore");
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Error creating ServerSocket");
        }

    }

    @Override
    public void interrupt() {
        Log.d(TAG, "Thread interrupted");
        tearDown();
        super.interrupt();
    }

    public void tearDown() {
        Log.d(TAG, "Try to clean used resources...");
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                Log.d(TAG, "ServerSocket has been closed");
            } catch (IOException e) {
                Log.d(TAG, "Cann't close ServerSocket. Leave as-is");
            }
            mServerSocket = null;
        }
        if(mClient!=null){
            Log.d(TAG, "Try to interrupte Client thread...");
            mClient.interrupt();
            mClient = null;
        }
    }

    private void sendMessage(String message){
        Log.d(TAG, "Try to send message:\n" + message);
        new MyHandler(mHandler).sendMessage(TAG, message);
    }
}
