package os.checkers.network2;

import android.util.Log;

import java.io.*;
import java.net.Socket;

class Connection {
    static final String TAG = Connection.class.getName();
    private final MyHandler outHandler;

    Connection(MyHandler handler) {
        assert handler != null;
        this.outHandler = handler;
        Log.d(TAG, Thread.currentThread().getName() + " has been started");
    }

    void receiveFrom(Socket socket) {
        new Thread(new ReceivingThread(outHandler, socket)).start();
    }

    void sendTo(Socket socket, String msg) {
        new Thread(new SendingThread(outHandler, socket, msg)).start();
    }

    static class ReceivingThread implements Runnable {
        public final static String TAG = ReceivingThread.class.getName();
        private Socket socket;
        private MyHandler outHandler;

        ReceivingThread(MyHandler handler, Socket socket) {
            assert socket != null;
            this.socket = socket;
            assert handler != null;
            this.outHandler = handler;
        }

        @Override
        public void run() {
            Log.d(TAG, Thread.currentThread().getName() + " has been started");
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder msg = new StringBuilder();
                String buffer;
                while ((buffer = input.readLine()) != null) {
                    msg.append(buffer);
                }
                input.close();
                outHandler.updatePosition(msg.toString());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                outHandler.sendMessage(HandlerType.ERROR, e.getMessage());
            }

            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                outHandler.sendMessage(HandlerType.ERROR, e.getMessage());
            }
        }
    }

    static class SendingThread implements Runnable {
        public final static String TAG = SendingThread.class.getName();
        private Socket socket;
        private MyHandler outHandler;
        private String msg;

        SendingThread(MyHandler outHandler, Socket socket, String msg) {
            assert msg != null;
            this.msg = msg;
            assert socket != null;
            this.socket = socket;
            assert outHandler != null;
            this.outHandler = outHandler;
        }

        @Override
        public void run() {
            Log.d(TAG, Thread.currentThread().getName() + " has been started");
            assert socket!=null;
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write(msg);
                outHandler.sendMessage(HandlerType.SENT, msg);
            } catch (IOException e) {
                Log.e(TAG, "send position error: " + e.getMessage());
                outHandler.sendError(e.getMessage());
            }
            try {
                if (out != null) {
                    out.close();
                }
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                outHandler.sendError(e.getMessage());
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if(socket!=null){
                if(!socket.isClosed()){
                    socket.close();
                }
                socket = null;
            }
            outHandler = null;
            super.finalize();
        }
    }
}