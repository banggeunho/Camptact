package io.camtact.android.presenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.camtact.android.model.MatchModel;
import io.camtact.android.mvp_interface.MatchMVP;
import io.camtact.android.service.MatchService;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class MatchPresenter implements MatchMVP.Presenter {

    private MatchMVP.View matchView;
    private MatchModel matchModel;

    private static final String TAG = "MatchPresenter";
    private final int TIMEOUT_SEC = 6;

    public static Thread timeCheckThread;
    public static boolean isThreadRunning;

    private Context context;
    private MatchService matchService;
    private boolean isBound = false;

    public MatchPresenter(MatchMVP.View view) {
        // View 연결
        matchView = view;
        // Model 연결
        matchModel = new MatchModel(this);
    }

    @Override
    public boolean checkOnlineStatus(Context context) {
        this.context = context;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void searchRandomUser() {
        // Bind to LocalService
        if(!isBound) {
            Intent intent = new Intent(context, MatchService.class);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        if(!isThreadRunning) {
            runTimeCheck();
        }
        matchModel.deleteOldChatRoom();
    }

    @Override
    public void stopMatch() {
        // Unbind from the service
        if (isBound) {
            context.unbindService(serviceConnection);
            isBound = false;
        }

        FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseuser!=null)
        matchModel.stopMatch(firebaseuser.getUid());
    }

    @Override
    public void checkIsSan() {
        matchModel.checkIsSan();
    }

    @Override
    public void isSearching() {
        matchModel.isSearching();
    }

    private void runTimeCheck() {
        timeCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {

                isThreadRunning = true;
                int sec = 0;

                while (isThreadRunning) {
                    try {
                        Thread.sleep(1000);
                        sec ++;
                        if(sec%TIMEOUT_SEC==0) {    //매 TIMEOUT_SEC 마다
                            matchView.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 바랍니다.");
                        }
                    } catch (InterruptedException e) {
                        isThreadRunning = false;
                        break;
                    }
                }
            }
        });
        timeCheckThread.setDaemon(true);
        timeCheckThread.start();
    }


    /**
     * call by Model
     */

    @Override
    public void showSnackBar(String msg) {
        matchView.showSnackBar(msg);
    }

    @Override
    public void createChatRoom(String matchedUid) {
        if (isBound) {
            // Call a method from the Service.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            matchService.createChatRoom(matchedUid);
            Log.e(TAG, "createChatRoom called");
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void randomMatchBtnOff() {
        matchView.randomMatchBtnOff();
    }

    @Override
    public void randomMatchBtnDisable() {
        matchView.randomMatchBtnDisable();
    }

    @Override
    public void randomMatchBtnEnable() {
        matchView.randomMatchBtnEnable();
    }

    @Override
    public void showProgressCircle() {
        matchView.showProgressCircle();
    }

    @Override
    public void hideProgressCircle() {
        matchView.hideProgressCircle();
    }

    @Override
    public void logout(boolean isSanctioned) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        matchView.goAuthActivity(isSanctioned);
    }







    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to Service, cast the IBinder and get Service instance
            MatchService.LocalBinder binder = (MatchService.LocalBinder) service;
            matchService = binder.getService();
            isBound = true;
            Log.e(TAG, "BindService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

}
