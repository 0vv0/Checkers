package os.checkers.network2;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    public static final String TAG = Server.class.getName();
    public static final String REQUESTED_BY = "requested by: ";
    public static final String LOCAL_ADDRESS = "local address: ";
    public static final String LOCAL_PORT = "local port: ";
    public static final String NO_PLAYER = "no player";
    public static final int port = 0;
    private ServerSocket mServerSocket;
    private MyHandler mHandler;
    private Connection connection;
    private InetAddress remoteAddress;
    private volatile int remotePort = 0;

    public Server(Handler handler) {
        assert handler != null;
        this.mHandler = new MyHandler(handler);
        connection = new Connection(mHandler);
    }

    public void send(String msg) {
        if (remoteAddress == null || remotePort == 0) {
            mHandler.sendMessage(TAG, NO_PLAYER);
            return;
        }
        connection.sendTo(remoteAddress, remotePort, msg);
    }

    public synchronized Server connect(InetAddress address, int port){
        if(remoteAddress==null){
            this.remoteAddress = address;
            this.remotePort = port;
        }
        return this;
    }


    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(port);
            Log.d(TAG, "Will listen on: " + mServerSocket.getInetAddress() + ":" + mServerSocket.getLocalPort());

            mHandler.sendMessage(TAG, LOCAL_PORT, mServerSocket.getLocalPort());
            mHandler.sendMessage(TAG, LOCAL_ADDRESS, mServerSocket.getInetAddress());

            while (!Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "ServerSocket Created, awaiting connection");
                Socket socket = mServerSocket.accept();
                String remotePlayer = socket.getInetAddress() + ":" + socket.getPort();
                mHandler.sendMessage(TAG, REQUESTED_BY + remotePlayer);
                Log.d(TAG, "Connect requested from: " + remotePlayer);
                if(remotePort==0) {
                    connect(socket.getInetAddress(), socket.getPort());
                }
                if(socket.getInetAddress().equals(remoteAddress)){
                    connection.receiveFrom(socket);
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
        connection = null;
    }

}
