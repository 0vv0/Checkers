package os.checkers.network3;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.*;
import java.net.*;

public class Player extends Service {
    public static final String TAG = Player.class.getName();
    public static final int SEND_POSITION = 0;
    private static final String PREFIX = "os.checkers.";

    public enum IntentAction {
        UPDATE_POSITION,
        REMOTE_PLAYER,
        REMOTE_PORT,
        LOCAL_PORT,
        RESTART_SERVICE;

        public static boolean contains(String action) {
            for (IntentAction intentAction : values()) {
                if (intentAction.name().equals(action)) {
                    return true;
                }
            }
            return false;
        }
    }

    private Server pServer;
    private Client pClient;
    private Socket pSocket;
    private int pPort = -1;

    private final IBinder pBinder;

    public Player() {
        Log.d(TAG, "Construct()");
        pServer = new Server();
        pBinder = new PlayerBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "bind(" + intent + ")");
        return pBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "bind(" + intent + ")");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasCategory(TAG)) {
            return super.onStartCommand(intent, flags, startId);
        }
        Log.d(TAG, "onStartCommand()");
        if (IntentAction.contains(intent.getAction())) {
            IntentAction action = IntentAction.valueOf(intent.getAction());
            switch (action) {
                case UPDATE_POSITION:
                    sendPosition(intent.getStringExtra(action.name()));
                    break;
                case REMOTE_PLAYER:
                    try {
                        String s = intent.getStringExtra(action.name());
                        int port = intent.getIntExtra(IntentAction.REMOTE_PORT.name(), -1);
                        if (pClient == null
                                && 0 < port && port <= 65535
                                && s != null) {
                            InetAddress address = InetAddress.getByName(s);
                            pClient = new Client(address, port);
                        }

                    } catch (UnknownHostException e) {
                        Log.d(TAG, "uhe\n" + e);
                    } catch (IOException e) {
                        Log.d(TAG, "ioe\n" + e);
                    }
                    break;
                case LOCAL_PORT:
                    if (-1 < pPort) {
                        Intent portIntent = new Intent(action.name());
                        intent.putExtra(action.name(), pPort);
                        sendBroadcast(portIntent);
                    }
                    break;
                case RESTART_SERVICE:
                    stopSelf();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        tearDown();
        super.onDestroy();
    }

    public void tearDown() {
        pServer.tearDown();
        if (pClient != null) {
            pClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) throws IOException {
        Log.d(TAG, "connectToServer:" + address + ":" + port);
        pClient = new Client(address, port);
    }

    public int getLocalPort() {
        return pPort;
    }

    public void setLocalPort(int localPort) {
        pPort = localPort;
    }

    public void sendPosition(String position) {
        if (pClient != null) {
            pClient.sendToOutput(position);
        }
    }

    private void sendBroadcast(String actionType, String data) {
        Log.d(TAG, actionType + "\n" + data);
        Intent intent = new Intent(actionType);
        intent.addCategory(TAG);
        intent.putExtra(actionType, data);
        sendBroadcast(intent);
    }

    private Socket getSocket() {
        return pSocket;
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "incoming connection from " + socket.getInetAddress() + ":" + socket.getPort());
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (pSocket != null) {
            if (pSocket.isConnected()) {
                try {
                    pSocket.close();
                } catch (IOException e) {
                    Log.d(TAG, "Cannot close Socket\n" + e);
                }
            }
            pSocket = socket;
        }
    }

    private class Server {
        final String TAG = Server.class.getName();
        Thread listenThread;
        private ServerSocket sServerSocket;

        public Server() {
            listenThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loopCode();
                }
            });
            listenThread.start();
            Log.d(TAG, "Construct()");
        }

        private void loopCode() {
            Log.d(TAG, Thread.currentThread().getName() + " has been started");
            try {
                sServerSocket = new ServerSocket(0, 2);
                Log.d(TAG, "Will listen on: " + sServerSocket.getInetAddress().getHostName() + ":" + sServerSocket.getLocalPort());
                setLocalPort(sServerSocket.getLocalPort());

                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "ServerSocket Created, awaiting connection");
                    setSocket(sServerSocket.accept());
                    Log.d(TAG, "Connected.");
                    if (pClient == null) {
                        int port = getSocket().getPort();
                        InetAddress address = getSocket().getInetAddress();
                        connectToServer(address, port);
                    }

                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket\n", e);
            }
            Log.d(TAG, Thread.currentThread().getName() + " run() complete");
        }

        private void tearDown() {
            Log.d(TAG, "tearDown(): Try to clean used resources...");
            listenThread.interrupt();
            if (sServerSocket != null) {
                try {
                    sServerSocket.close();
                    Log.d(TAG, "ServerSocket has been closed");
                } catch (IOException e) {
                    Log.d(TAG, "Cann't close ServerSocket. Leave as-is");
                }
                sServerSocket = null;
            }
        }

    }

    private class Client {
        final String TAG = Client.class.getName();
        private InetAddress cAddress;
        private int cPort;

        private HandlerThread cSendThread;
        private Thread cReceiveThread;
        private Handler cHandler;

        Client(InetAddress address, int port) throws IOException {
            Log.d(TAG, "Construct(" + address + ":" + port + ")");
            cAddress = address;
            cPort = port;
            if (getSocket() == null) {
                setSocket(new Socket(cAddress, cPort));
                Log.d(TAG, "Client-side socket initialized.");
            } else {
                Log.d(TAG, "Socket already initialized. skipping!");
            }
            cSendThread = new SenderThread(getSocket().getOutputStream());
            cSendThread.start();
            cHandler = new Handler(cSendThread.getLooper());

            cReceiveThread = new Thread(new ReceiverThread(getSocket().getInputStream()));
            cReceiveThread.start();

            Intent intent = new Intent(IntentAction.REMOTE_PLAYER.name());
            intent.addCategory(TAG);
            intent.putExtra(IntentAction.REMOTE_PLAYER.name(), address.getHostAddress());
            intent.putExtra(IntentAction.REMOTE_PORT.name(), port);
            sendBroadcast(intent);
        }

        void sendToOutput(String data) {
            cHandler.obtainMessage(SEND_POSITION, data).sendToTarget();
        }

        public void tearDown() {
            cSendThread.quit();
            if (cReceiveThread != null) {
                if (cReceiveThread.isAlive()) {
                    cReceiveThread.interrupt();
                }
            }
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        private class SenderThread extends HandlerThread implements Handler.Callback {
            final String TAG = SenderThread.class.getName();
            private BufferedWriter out;

            SenderThread(OutputStream outputStream) {
                super(SenderThread.class.getName());
                out = new BufferedWriter(new OutputStreamWriter(outputStream));
            }

            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == SEND_POSITION) {
                    try {
                        out.write((String) msg.obj);
                        out.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "send position error\n" + e);
                    }
                }
                return false;
            }
        }

        private class ReceiverThread implements Runnable {
            final String TAG = ReceiverThread.class.getName();
            BufferedReader in;

            ReceiverThread(InputStream in) {
                this.in = new BufferedReader(new InputStreamReader(in));
            }

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        StringBuffer sb = new StringBuffer();
                        do {
                            sb.append(in.readLine());
                        } while (in.ready());
                        sendBroadcast(IntentAction.UPDATE_POSITION.name(), sb.toString());
                    } catch (IOException e) {
                        Log.d(TAG, "read position error\n" + e);
                    }
                }
            }
        }
    }

    public class PlayerBinder extends Binder {
        public Player getService() {
            return Player.this;
        }
    }
}
