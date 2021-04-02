package io.camtact.android.mvp_interface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import io.camtact.android.model.dto.ChatRoom;

public interface ChatMVP {

    interface View {

        void setFriendNameText(String userName);

        void addMsg(ChatRoom.Chat chat);

        void removeAllMsg();

        void showProgressBar();

        void hideProgressBar();

        void showSnackBar(String message, int milliTime, boolean onTheTop);

        void setLeftTime(String leftTime);

        void finishActivity(int reason);
    }

    interface Presenter {

        boolean checkOnlineStatus(Context context);

        void getIntent(Intent intent);

        void applyFriendNameAndReadMsg();

        void checkChatRoom();

        void checkMessage(String msg);

        void removeWholeListener();

        void seenMessage();

        void readMessage();

        void showLeftTime();

        void getImage(int requestCode, int resultCode, Intent data, int IMAGE_REQUEST, Activity activity);

        void report(String reason);

        void deleteChatRoom();


        //call by Model
        void setFriendNameText(String userName);

        void removeAllMsg();

        void addMsg(ChatRoom.Chat chat);

        String getFileExtension(Uri uri, Activity activity);

        void hideProgressBar();

        //void deleteFileFromMediaStore(ContentResolver contentResolver, File file);

        void runTimeChecker(long madeTime);

        void showSnackBar(String message, int milliTime, boolean onTheTop);

        void finishActivity(int reason);
    }

    interface Model {

        void applyFriendNameAndReadMsg(String friendUid);

        void checkChatRoom();

        void seenMessage();

        void readMessage();

        void showLeftTime();

        void removeWholeListener();

        void sendMessage(String sender, String message);

        void uploadImage(Activity activity, Uri imageUri, File toDeleteFile);

        void report(String reason);

        void deleteChatRoom(boolean myself);
    }
}
