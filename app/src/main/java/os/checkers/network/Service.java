/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package os.checkers.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Service extends IntentService {
    public enum Intents{
        LIST_PLAYERS,
        USE_PLAYER,
        SET_POSITION,
        GET_POSITION;
        static boolean containsName(String name) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    private static final String TAG = Service.class.getName();

    private NsdHelper mNsdHelper;
    private Handler mUpdateHandler;
    private Connection mConnection;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Service(final String name) {
        super(name);
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent();
                intent.setAction(IntentActions.SET_POSITION.name());
                intent.putExtra("data", msg.getData());
                sendBroadcast(intent);
            }
        };
    }

    public void clickAdvertise(View v) {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }
    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }
    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }
    public void Send(final String msg) {
        if (msg != null) {
            if (!msg.isEmpty()) {
                mConnection.sendMessage(msg);
            }
        }
    }
    protected void onStart() {
        Log.d(TAG, "Starting.");
        mConnection = new Connection(mUpdateHandler);
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
    }
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
    }
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }
    // For KitKat and earlier releases, it is necessary to remove the
    // service registration when the application is stopped.  There's
    // no guarantee that the onDestroy() method will be called (we're
    // killable after onStop() returns) and the NSD service won't remove
    // the registration for us if we're killed.
    // In L and later, NsdService will automatically unregister us when
    // our connection goes away when we're killed, so this step is
    // optional (but recommended).
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        mNsdHelper.tearDown();
        mConnection.tearDown();
        mNsdHelper = null;
        mConnection = null;
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_LONG).show();
        switch (Intents.valueOf(intent.getAction())){
            case USE_PLAYER:
                break;
            case LIST_PLAYERS:
                break;
            case SET_POSITION:
                break;
            case GET_POSITION:
                break;
        }
    }
}