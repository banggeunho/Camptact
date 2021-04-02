package io.camtact.android.mvp_interface;


import android.content.Context;

import io.camtact.android.model.dto.ChatRoom;

public interface ChatRoomMVP {

    interface View {

        void addChatRoom(ChatRoom chatRoom);

        void removeAll();

        void getRoomCount();
    }

    interface Presenter {
        boolean checkOnlineStatus(Context context);

        void addChatRoomList();

        void removeAll();

        void addChatRoom(ChatRoom chatRoom);

        void getRoomCount();

        void removeListener();
    }

    interface Model {

        void addChatRoomList();

        void removeListener();
    }
}
