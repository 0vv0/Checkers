package os.checkers.network2;

import android.support.constraint.BuildConfig;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    public static final String TAG = Server.class.getName();

    private ServerSocket mServerSocket;

    private MyHandler outHandler;

    public Server(MyHandler outHandler) throws IllegalArgumentException {
        super(TAG);
        Log.d(TAG, Thread.currentThread().getName() + " has been started");
        if(BuildConfig.DEBUG&&outHandler==null){throw new AssertionError("outHandler is NUll???");}

        this.outHandler = outHandler;
    }

    @Override
    public void run() {
        loopCode();
    }

    private void loopCode() {
        Log.d(TAG, Thread.currentThread().getName() + " has been started");
        try {
            if (mServerSocket == null) {
                mServerSocket = new ServerSocket(0, 2);
                Log.d(TAG, "Will listen on: " + mServerSocket.getInetAddress().getHostName() + ":" + mServerSocket.getLocalPort());
                outHandler.sendMessage(HandlerType.LOCAL_PLAYER, new Player(mServerSocket.getInetAddress().getHostName(), mServerSocket.getLocalPort()));
            }
            Log.d(TAG, "ServerSocket Created, awaiting connection");
            while (!isInterrupted()) {
                Socket socket = mServerSocket.accept();
                new Connection(outHandler).receiveFrom(socket);
            }
        } catch (IOException e) {
            if (!this.isInterrupted()) {
                Log.d(TAG, "Error creating ServerSocket");
            }
        }
    }

    @Override
    public void interrupt() {
        Log.d(TAG, "Thread interrupted");
        super.interrupt();
        tearDown();
    }

    private void tearDown() {
        Log.d(TAG, "tearDown(): Try to clean used resources...");
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                Log.d(TAG, "ServerSocket has been closed");
            } catch (IOException e) {
                Log.d(TAG, "Cann't close ServerSocket. Leave as-is");
            }
            mServerSocket = null;
        }
    }


}
