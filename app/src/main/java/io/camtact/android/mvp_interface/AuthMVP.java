package io.camtact.android.mvp_interface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public interface AuthMVP {

    interface View {
        void showSnackBar(String message, int milliTime, boolean simple, boolean onTheTop);

        void hideKeyboard();

        void startMainActivity();

        void showAlertDialog(String email);

        void showBanDialog();

        void showProgressBar();

        void hideProgressBar();

        void disableSendBtn();

        void enableSendBtn();
    }

    interface Presenter {
        //call by View
        boolean checkOnlineStatus(Context context);

        void initFirebaseAuth();

        void sendSignInLink(String email);

        void checkEmailAddress(String email);

        void checkIntent(Intent intent);

        boolean intentHasEmailLink(Intent intent);

        void signInWithEmailLink(String email, String link);

        void autoLogin();

        void outState(Bundle outState);

        void savedInstanceState(Bundle savedInstanceState);

        void saveSharedPreferences(Activity activity);

        void openSharedPreferences(Activity activity);

        //call by Model
        void openSavedPendingEmail(String savedPendingEmail);

        void startMainActivity();

        void showSnackbar(String message, int milliTime, boolean simple, boolean onTheTop);
    }

    interface Model {
        void saveSharedPreferences(String pendingEmail, Activity activity);

        void openSharedPreferences(Activity activity);

        void signInUser(String userUid, String userName, String email);
    }
}
