package os.checkers.network2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RemotePlayer extends Service {
    private Server server;
    public RemotePlayer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
