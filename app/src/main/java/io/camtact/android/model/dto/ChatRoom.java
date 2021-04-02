package io.camtact.android.model.dto;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;

@Keep
public class ChatRoom {

    //public Map<String, Object> info = new HashMap<>();
    public Info info;
    public Map<String, Chat> chats = new HashMap<>();//채팅방의 대화내용

    public ChatRoom() {

    }

    public ChatRoom(Info info, Map<String, Chat> chats) {
        this.info = info;
        this.chats = chats;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Map<String, Chat> getChats() {
        return chats;
    }

    public void setChats(Map<String, Chat> chats) {
        this.chats = chats;
    }

    @Keep
    public static class Info {
        private Object madeTime;
        private String user1Uid;
        private String user2Uid;
        private String user1Name;
        private String user2Name;

        public Info() {

        }

        public Info(long madeTime, String user1Uid, String user2Uid, String user1Name, String user2Name) {
            this.madeTime = madeTime;
            this.user1Uid = user1Uid;
            this.user2Uid = user2Uid;
            this.user1Name = user1Name;
            this.user2Name = user2Name;
        }

        public Object getMadeTime() {
            return madeTime;
        }

        public void setMadeTime(Object madeTime) {
            this.madeTime = madeTime;
        }

        public String getUser1Uid() {
            return user1Uid;
        }

        public void setUser1Uid(String user1Uid) {
            this.user1Uid = user1Uid;
        }

        public String getUser2Uid() {
            return user2Uid;
        }

        public void setUser2Uid(String user2Uid) {
            this.user2Uid = user2Uid;
        }

        public String getUser1Name() {
            return user1Name;
        }

        public void setUser1Name(String user1Name) {
            this.user1Name = user1Name;
        }

        public String getUser2Name() {
            return user2Name;
        }

        public void setUser2Name(String user2Name) {
            this.user2Name = user2Name;
        }
    }

    @Keep
    public static class Chat {

        public String sender;
        public String message;
        public boolean isSeen;
        public Object time;
        public String imageURL;
        public boolean notice;

        public Chat() {
        }

        public Chat(String sender, String message, boolean isSeen, Object time, String imageURL, boolean notice) {
            this.sender = sender;
            this.message = message;
            this.isSeen = isSeen;
            this.time = time;
            this.imageURL = imageURL;
            this.notice = notice;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isIsSeen() {
            return isSeen;
        }

        public void setIsSeen(boolean isSeen) {
            this.isSeen = isSeen;
        }

        public Object getTime() {
            return time;
        }

        public void setTime(Object time) {
            this.time = time;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public boolean isNotice() {
            return notice;
        }

        public void setNotice(boolean notice) {
            this.notice = notice;
        }
    }
}
