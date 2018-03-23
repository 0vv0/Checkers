package os.checkers.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Connection{
    private GameServer mGameServer;
    private GameClient mGameClient;
    public static final String TAG = Connection.class.getName();
    public static final String PORT = "port";
    public static final String POSITION = "position";
    private static Socket mSocket;
    private int mPort = -1;
    private Handler mHandler;
    public volatile String position = "";
    public Connection(Handler mHandler) {
        super();
        mGameServer = new GameServer();
        this.mHandler = mHandler;
    }
    public void tearDown() {
        mGameServer.tearDown();
        if (mGameClient != null) {
            mGameClient.tearDown();
        }
    }
    public void connectToServer(InetAddress address, int port) {
        mGameClient = new GameClient(address, port);
    }
    public void sendMessage(String msg) {
        if (mGameClient != null) {
            mGameClient.sendMessage(msg);
        }
    }
    public int getLocalPort() {
        return mPort;
    }
    public void setLocalPort(int port) {
        mPort = port;
        updateMessages(PORT);
    }
    public void updateMessages(String msg) {
        Log.e(TAG, msg);
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(TAG, msg);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }
    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
        mSocket = socket;
    }
    private Socket getSocket() {
        return mSocket;
    }
    public void setPosition(String position){
        this.position = position;
        updateMessages(POSITION);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "mGameServer=" + mGameServer +
                ", mGameClient=" + mGameClient +
                ", mSocket=" + mSocket +
                ", mPort=" + mPort +
                ", mHandler=" + mHandler +
                '}';
    }

    private class GameServer {
        @Override
        protected void finalize() throws Throwable {
            try {
                if(mServerSocket!=null) {
                    mServerSocket.close();
                }
                if(mThread!=null){
                    mThread.interrupt();
                }
            } finally {
                super.finalize();
            }
        }

        ServerSocket mServerSocket = null;
        Thread mThread = null;
        GameServer() {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }
        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }
        class ServerThread implements Runnable {
            @Override
            public void run() {
                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        if (mGameClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            Log.d(TAG, mServerSocket.toString());
                            Log.d(TAG, mSocket.toString());

                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }
    private class GameClient {
        @Override
        protected void finalize() throws Throwable {
            try {
                if(mSendThread!=null){
                    mSendThread.interrupt();
                }
                if(mRecThread!=null){
                    mRecThread.interrupt();
                }
                tearDown();
            }finally {
                super.finalize();
            }
        }

        private InetAddress mAddress;
        private int PORT;
        private final String CLIENT_TAG = "GameClient";
        private Thread mSendThread;
        private Thread mRecThread;
        GameClient(InetAddress address, int port) {
            Log.d(CLIENT_TAG, "Creating GameClient");
            this.mAddress = address;
            this.PORT = port;
            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }
        class SendingThread implements Runnable {
            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;
            SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
            }
            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }
                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();
                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }
                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }
        class ReceivingThread implements Runnable {
            @Override
            public void run() {
                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        String msg = "";
                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
                            msg+=messageStr;
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!");
                            break;
                        }
                        setPosition(msg);
                    }
                    input.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }
        void tearDown() {
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }
        void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        try {
            if(mSocket!=null){
                mSocket.close();
            }
        }finally {
            super.finalize();
        }
    }
}