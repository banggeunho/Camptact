package io.camtact.android.mvp_interface;

import android.content.Context;

public interface SetMVP {

    interface View{

        void setUserName(String userName);

        void hideProgressBar();

        void goAuthActivity(boolean isLeaving);

        void showLogoutDialog();

        void showSnackBar(String message, int milliTime, boolean onTheTop);

    }

    interface Presenter {

        boolean checkOnlineStatus(Context context);

        void getUserName();

        void editUserName(String userName);

        void outOfMembership();

        void logout(boolean isLeaving);

        void sendInquiry(String inquiry);

        void sendError(String error);

        //call by Model

        void setUserName(String userName);

        void hideProgressBar();

        void showLogoutDialog();

        void showSnackBar(String message, int milliTime, boolean onTheTop);

    }

    interface Model {
        void getUserName();

        void editUserName(String userName);

        void outOfMembership();

        void sendInquiry(String inquiry);

        void reportError(String error);
    }
}
