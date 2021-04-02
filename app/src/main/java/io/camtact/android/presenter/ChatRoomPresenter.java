package io.camtact.android.presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.camtact.android.model.ChatRoomModel;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.mvp_interface.ChatRoomMVP;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class ChatRoomPresenter implements ChatRoomMVP.Presenter {

    private ChatRoomMVP.View chatRoomView;
    private ChatRoomModel chatRoomModel;

    public ChatRoomPresenter(ChatRoomMVP.View view) {
        // View 연결
        chatRoomView = view;
        chatRoomModel = new ChatRoomModel(this);
    }

    @Override
    public boolean checkOnlineStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void addChatRoomList() {

        chatRoomModel.addChatRoomList();
    }

    //call by Model
    @Override
    public void removeAll() {
        chatRoomView.removeAll();
    }

    @Override
    public void addChatRoom(ChatRoom chatRoom) {
        chatRoomView.addChatRoom(chatRoom);
    }

    @Override
    public void getRoomCount() {
        chatRoomView.getRoomCount();
    }

    @Override
    public void removeListener() {
        chatRoomModel.removeListener();
    }
}
