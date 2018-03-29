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
    private Connection connection;

    private InetAddress remoteAddress;
    private volatile int remotePort = DEFAULT_PORT;

    private MyHandler outHandler;
    private volatile MyHandler inHandler;

    public Server(Handler handler) {
        super(TAG);
        assert handler != null;
        this.outHandler = new MyHandler(handler);
        connection = new Connection(outHandler);
    }

    public Server(MyHandler handler) {
        super(TAG);
        assert handler != null;
        this.outHandler = handler;
        connection = new Connection(outHandler);
    }

    private synchronized MyHandler getInHandler() {
        return inHandler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        inHandler = new MyHandler(new Handler(getLooper(), this));
    }

    private void send(String msg) {
        if (remotePort == DEFAULT_PORT||remoteAddress == null) {
            outHandler.sendMessage(HandlerType.NO_PLAYER);
            return;
        }
        try {
            connection.sendTo(new Socket(remoteAddress, remotePort), msg);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            outHandler.sendMessage(HandlerType.ERROR, e.getMessage());
        }
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
    }

    private void connectRequestedFrom(Socket socket) {
        Log.d(TAG, "Connect requested from: " + socket.getInetAddress());
        if (remotePort == 0) {
            outHandler.sendMessage(HandlerType.REMOTE_HOST, socket.getInetAddress().getHostName());
            outHandler.sendMessage(HandlerType.REMOTE_PORT, String.valueOf(socket.getPort()));

        } else if (socket.getInetAddress().equals(remoteAddress)) {
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
                outHandler.sendMessage(type, mServerSocket.getInetAddress().getHostName());
                break;
            case LOCAL_PORT:
                outHandler.sendMessage(type, String.valueOf(mServerSocket.getLocalPort()));
                break;
            case REMOTE_HOST:
                setRemoteAddress(bundle.getString(type.name(), null));
                break;
            case REMOTE_PORT:
                setRemotePort(bundle.getInt(type.name(), 0));
                break;
            case NO_PLAYER:
            case SENT:
            case ERROR:
                outHandler.sendMessage(type);
                break;
            case UPDATE_POSITION:
                String s = bundle.getString(type.name(), "");
                if (!s.isEmpty()) {
                    send(s);
                }
                break;

        }
    }

    public void addRequest(HandlerType type, String data) {
        getInHandler().sendMessage(type, data);
    }
}
