package io.camtact.android.mvp_interface;

import android.content.Context;

public interface MatchMVP {

    interface View {

        void showSnackBar(String msg);

        void randomMatchBtnOff();

        void randomMatchBtnDisable();

        void randomMatchBtnEnable();

        void showProgressCircle();

        void hideProgressCircle();

        void goAuthActivity(boolean isSanctioned);
    }

    interface Presenter {

        boolean checkOnlineStatus(Context context);

        void searchRandomUser();

        void stopMatch();

        void isSearching();

        void checkIsSan();

        void showProgressCircle();

        void hideProgressCircle();

        //called by Model
        void createChatRoom(String matchedUid);

        void randomMatchBtnOff();

        void randomMatchBtnDisable();

        void randomMatchBtnEnable();

        void logout(boolean isSanctioned);

        void showSnackBar(String msg);
    }

    interface Model {

        void deleteOldChatRoom();

        void stopMatch(String uid);

        void isSearching();

        void checkIsSan();
    }
}
