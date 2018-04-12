package os.checkers.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListener,String ipAddress, int port) throws IOException {
        this(eventListener, new Socket(ipAddress, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new ReceiveThread(eventListener));
        rxThread.start();
    }

    public synchronized void sendData(String value) {
        try {
            out.write(value);
            out.flush();
        } catch (IOException e) {
            eventListener.onError(TCPConnection.this, e);
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onError(TCPConnection.this, e);
        }
        eventListener.onDisconnect(TCPConnection.this);
    }

    private class ReceiveThread implements Runnable {
        private final TCPConnectionListener eventListener;

        ReceiveThread(TCPConnectionListener eventListener) {
            this.eventListener = eventListener;
        }

        @Override
        public void run() {
            eventListener.onReady(TCPConnection.this);
            try {
                while (!rxThread.isInterrupted()) {
                    StringBuilder buffer = new StringBuilder();
                    String s;
                    while ((s = in.readLine()) != null) {
                        buffer.append(s);
                    }
                    s = buffer.toString();
                    if(!s.isEmpty()) {
                        eventListener.onReceive(TCPConnection.this, s);
                    }
//                    yield();
                }
            } catch (IOException e) {
                eventListener.onError(TCPConnection.this, e);
            } finally {
                eventListener.onDisconnect(TCPConnection.this);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }
}
