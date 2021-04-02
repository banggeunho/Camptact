package io.camtact.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import io.camtact.android.view.ChatActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class MatchService extends Service {

    private static final String TAG = "MatchService";

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MatchService getService() {
            // Return this instance of Service so clients can call public methods
            return MatchService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    /** method for clients */

    public void createChatRoom(String matchedUid) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("uid", matchedUid);
        startActivity(intent);

        Vibrator vibrator;
        vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(700);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "called onDestroy");
    }
}
