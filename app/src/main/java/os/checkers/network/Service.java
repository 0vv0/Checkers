package os.checkers.network;

import android.app.IntentService;
import android.content.Intent;

public class Service extends IntentService {
    public Service() {
        this(Service.class.getName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Service(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent broadcastIntent = new Intent();

        switch (IntentActions.valueOf(intent.getAction())) {
            case LIST_PLAYERS:
                broadcastIntent.setAction(IntentActions.LIST_PLAYERS.name());
                sendBroadcast(broadcastIntent);
                break;
            case SET_POSITION:
                break;
            case GET_POSITION:
                break;
        }
    }

}
