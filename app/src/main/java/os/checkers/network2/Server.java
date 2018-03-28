package os.checkers.network2;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server extends HandlerThread implements Handler.Callback {
    public static final String TAG = Server.class.getName();
    public static final int DEFAULT_PORT = 0;

    private ServerSocket mServerSocket;
    private MyHandler mHandler;
    private Connection connection;
    private InetAddress remoteAddress;
    private int remotePort = 0;
    private volatile MyHandler inHandler;

    private synchronized MyHandler getInHandler(){
        return inHandler;
    }

    public Server(Handler handler) {
        super(TAG);
        assert handler != null;
        this.mHandler = new MyHandler(handler);
        connection = new Connection(mHandler);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        inHandler = new MyHandler(new Handler(getLooper(),this));
    }

    private void send(String msg) {
        if (remoteAddress == null || remotePort == 0) {
            mHandler.sendMessage(HandlerType.NO_PLAYER, "");
            return;
        }
        connection.sendTo(remoteAddress, remotePort, msg);
    }

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(DEFAULT_PORT);
            Log.d(TAG, "Will listen on: " + mServerSocket.getInetAddress().getHostName() + ":" + mServerSocket.getLocalPort());

            while (!Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "ServerSocket Created, awaiting connection");
                Socket socket = mServerSocket.accept();
                connectRequestedFrom(socket);
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

    private void tearDown() {
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
        remoteAddress = null;
    }

    private void connectRequestedFrom(Socket socket){
        Log.d(TAG, "Connect requested from: " + socket.getInetAddress());
        if(remotePort==0){
            Bundle bundle = new Bundle();
            bundle.putString(HandlerType.REMOTE_HOST.name(), socket.getInetAddress().getHostName());
            bundle.putInt(HandlerType.REMOTE_PORT.name(),socket.getPort());
            mHandler.sendMessage(bundle);
        }
        if (socket.getInetAddress().equals(remoteAddress)) {
            connection.receiveFrom(socket);
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        for (HandlerType t : HandlerType.values()) {
            if (bundle.containsKey(t.name())) {
                dispatcher(t, bundle);
            }
        }
        return false;
    }

    private synchronized void setRemoteAddress(String address) {
        try {
            if (address != null) {
                this.remoteAddress = InetAddress.getByName(address);
            }
        } catch (UnknownHostException e) {
            Log.d(TAG, "UHE\n" + e.getMessage());
        }
    }

    private synchronized void setRemotePort(int port) {
        if (port != 0) {
            this.remotePort = port;
        }
    }

    private void dispatcher(HandlerType type, Bundle bundle) {
        switch (type) {
            case LOCAL_HOST:
                mHandler.sendMessage(type, mServerSocket.getInetAddress().getHostName());
                break;
            case LOCAL_PORT:
                mHandler.sendMessage(type, String.valueOf(mServerSocket.getLocalPort()));
                break;
            case REMOTE_HOST:
                setRemoteAddress(bundle.getString(type.name(), null));
                break;
            case REMOTE_PORT:
                setRemotePort(bundle.getInt(type.name(), 0));
                break;
            case NO_PLAYER:
            case SENT:
                mHandler.sendMessage(type, "");
                break;
            case UPDATE_POSITION:
                String s = bundle.getString(type.name(), "");
                if (!s.isEmpty()) {
                    send(s);
                }
                break;
        }
    }

   public void addRequest(HandlerType type, String data){
        getInHandler().sendMessage(type, data);
   }
}
