package os.checkers.network2;

import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Connection{
    public static final String TAG = Connection.class.getName();
    private final MyHandler mHandler;

    public Connection(MyHandler handler) {
        assert handler != null;
        this.mHandler = handler;
    }

    public void receiveFrom(Socket socket){
        new Thread(new ReceivingThread(mHandler, socket)).start();
    }

    public void sendTo(Socket socket, String msg){
        new Thread(new SendingThread(mHandler, socket, msg)).start();
    }

//    private static synchronized void sendMessage(Handler handler, String message) {
//        Log.d(TAG, "Try to send message:\n" + message);
//        new MyHandler(handler).sendMessage(TAG, message);
//    }

    public void sendTo(InetAddress remoteAddress, int remotePort, String msg) {
        try {
            Socket socket = new Socket(remoteAddress, remotePort);
            sendTo(socket, msg);
        } catch (IOException e) {
            Log.d(TAG, "Can't create Socket to " + remoteAddress.getHostAddress() + ":" + remotePort + "\n" + e.getMessage());
        }

    }

    public static class ReceivingThread implements Runnable {
        public final static String TAG = ReceivingThread.class.getName();
        private Socket socket;
        private MyHandler handler;

        ReceivingThread(MyHandler handler, Socket socket) {
            assert socket!=null;
            this.socket = socket;
            assert handler!=null;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder msg = new StringBuilder();
                String line = input.readLine();
                while (line!=null) {
                    msg.append(line);
                    line = input.readLine();
                }
                input.close();
                socket.close();
                handler.sendMessage(Type.UPDATE_POSITION, msg);
            } catch (IOException e) {
                Log.e(TAG, "loop error: ", e);
            }
        }
    }

    public static class SendingThread implements Runnable{
        public final static String TAG = SendingThread.class.getName();
        private Socket socket;
        private MyHandler handler;
        private String msg;

        SendingThread(MyHandler handler, Socket socket, String msg) {
            assert msg!=null;
            this.msg = msg;
            assert socket!=null;
            this.socket = socket;
            assert handler!=null;
            this.handler =handler;
        }

        @Override
        public void run() {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write(msg);
                out.close();
                socket.close();
                handler.sendMessage(Type.SENT, msg);
            } catch (IOException e) {
                Log.e(TAG, "loop error: ", e);
            }
        }
    }
}