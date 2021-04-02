package io.camtact.android.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.model.dto.User;
import io.camtact.android.mvp_interface.MatchMVP;

import static io.camtact.android.presenter.ChatPresenter.TIME_LIMIT;
import static io.camtact.android.presenter.MatchPresenter.isThreadRunning;
import static io.camtact.android.presenter.MatchPresenter.timeCheckThread;

public class MatchModel implements MatchMVP.Model {

    private MatchMVP.Presenter matchPresenter;

    private static final String TAG = "MatchModel";
    private final int MAX_CHAT_ROOM = 0;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference reference;
    private static DatabaseReference searchReference;
    private DatabaseReference matchWhoRef;
    private static ChildEventListener searchListener;

    public static boolean requestMatch;
    private boolean amIMatched;
    private String myName;

    public MatchModel(MatchMVP.Presenter presenter) {
        this.matchPresenter = presenter;
    }

    @Override
    public void deleteOldChatRoom() {

        /*if(myName == null) {
            matchPresenter.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 바랍니다.");
            isSearching();
        } else {*/
            //중복입력 오류방지 위해 버튼 막기
            matchPresenter.randomMatchBtnDisable();

            searchRandomUser(myName); //이 부분이 아래 requestMatch와 같이 호출되면, 상대가 나를 get해도
                                    // 아직 addChildEventListener가 발동하지 않아서, 나만 채팅 시작이 안됌.

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {    //ChatRooms가 존재하지않는다면(지금 바로 매칭시작)
                        Log.e(TAG, "howManyChat0");
                        requestMatch();
                    } else {
                        int chatRoomEA = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                            if (chatRoom != null) {
                                ChatRoom.Info info = chatRoom.getInfo();

                                if (info != null && info.getUser1Uid().equals(firebaseUser.getUid())
                                        || info != null && info.getUser2Uid().equals(firebaseUser.getUid())) {    //내가 속한 채팅방 찾기

                                    chatRoomEA ++;
                                    final String chatRoomUid = snapshot.getKey();

                                    if (chatRoomUid != null) {
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("info");
                                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                ChatRoom.Info info = dataSnapshot.getValue(ChatRoom.Info.class);
                                                if (info != null) {
                                                    long madeTime = Long.parseLong(String.valueOf(info.getMadeTime())); //Object를 long으로 파싱

                                                    calculateLeftTime(madeTime, chatRoomUid);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if(chatRoomEA==0) {
                            Log.e(TAG, "No ChatRoom at me. or Error : Not info. Only Chats exist");
                            requestMatch();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        //}
    }

    private void requestMatch() {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("matchedWho", null);
        hashMap.put("requestMatch", true);
        hashMap.put("getKicked", false);

        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    requestMatch = true;
                    matchPresenter.randomMatchBtnEnable();
                    matchPresenter.showProgressCircle();
                } else {
                    Log.e("MatchModel", "requestMatch().addOnFailureListener");
                    isSearching();
                    matchPresenter.randomMatchBtnOff(); //매치 검색버튼 검색해제 상태로 만들기
                    matchPresenter.randomMatchBtnEnable();
                }
                if (isThreadRunning)
                    timeCheckThread.interrupt();
            }
        });
    }

    @Override
    public void isSearching() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    myName = user.getUserName();
                    Log.e(TAG, "myName :" + myName);

                    if(myName == null) {
                        matchPresenter.logout(false);
                    } else if (user.isRequestMatch()) {
                        requestMatch = true;
                        matchPresenter.showProgressCircle();
                    } else if (!user.isRequestMatch()) {
                        requestMatch = false;
                        matchPresenter.randomMatchBtnOff();
                        if(searchListener != null) {
                            searchReference.removeEventListener(searchListener);
                            searchListener = null;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void stopMatch(String uid) {
        if(searchListener != null) {
            searchReference.removeEventListener(searchListener);
            searchListener = null;
        }

        //여러입력 오류방지 위해 버튼 막기
        matchPresenter.randomMatchBtnDisable();
        //deleteMatchOrder();

        reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        HashMap<String, Object> hashMap = new HashMap<>();
        //hashMap.put("matchedWho", null);
        hashMap.put("requestMatch", false);
        //DB의 매치요청상태를 false로 바꿔줌
        reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                requestMatch = false;
                matchPresenter.randomMatchBtnOff();
                matchPresenter.randomMatchBtnEnable();
                matchPresenter.hideProgressCircle();
            }
        });
    }

    private void searchRandomUser(final String myName) {
        amIMatched = false;
        if(searchListener == null) {

            searchReference = FirebaseDatabase.getInstance().getReference("Users");
            searchListener = searchReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.e("MatchModel", "onChildChanged:" + dataSnapshot.getKey() + " amIMatched :" + amIMatched);

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null && user.getUid().equals(firebaseUser.getUid()) && user.getMatchedWho() != null && !amIMatched) {   //내가 누구와 매칭됬다면

                        Log.e("MatchModel", "matched by others");
                        amIMatched = true;
                        stopMatch(firebaseUser.getUid());   //내 자신의 매칭요청 일단 중지시킴
                        createChatRoom(user.getMatchedWho());
                        Log.e("MatchModel", "createChatRoom by others :" + user.getMatchedWho());
                    } else if (!amIMatched && requestMatch && user != null && !user.getUid().equals(firebaseUser.getUid())//!amIMatched >> 내가 아직 매치가 안됬어야
                            && user.isRequestMatch() && user.getMatchedWho() == null) { //내가 아닌 유저가 랜덤챗 매치요청 상태라면

                        amIMatched = true;

                        matchedWith(user.getUid(), firebaseUser.getUid());//매칭된 나의 Uid를 상대 DB에 업뎃
                        stopMatch(firebaseUser.getUid());

                        //첫메세지를 매칭 알림 메세지를 보냄으로써, 채팅방 목록 DB에 추가됨
                        sendNoticeMsg(firebaseUser.getUid(), user.getUid(), myName, user.getUserName());
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("MatchModel", "ChildEventListener - onChildRemoved : " + dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.e("MatchModel", "ChildEventListener - onChildMoved" + s);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MatchModel", "ChildEventListener - onCancelled" + databaseError.getMessage());
                }

            });
        }
    }

    private void matchedWith(String userUid, final String matchedUid) {
        matchWhoRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("matchedWho", matchedUid);
        //매칭된 상대 Uid를 업뎃해줌.
        matchWhoRef.updateChildren(hashMap);
    }

    private void createChatRoom(String matchedUid) {
        matchPresenter.createChatRoom(matchedUid);
        matchPresenter.randomMatchBtnOff(); //매치 검색버튼 검색해제 상태로 만들기
    }

    private void sendNoticeMsg(final String sender, final String receiver, String myName, String friendName) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms");

        ChatRoom.Info info = new ChatRoom.Info();
        info.setMadeTime(ServerValue.TIMESTAMP);
        info.setUser1Uid(sender);
        info.setUser1Name(myName);
        info.setUser2Uid(receiver);
        info.setUser2Name(friendName);

        ChatRoom.Chat chat = new ChatRoom.Chat();
        chat.setSender(sender);
        chat.setMessage("새로운 상대와 만났습니다. 즐거운 매너 채팅되세요:)");
        chat.setIsSeen(false);
        chat.setTime(ServerValue.TIMESTAMP);
        chat.setImageURL(null);
        chat.setNotice(true);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.info = info;
        chatRoom.chats.put("-0notice", chat);

        reference.push().setValue(chatRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                createChatRoom(receiver);
            }
        });
    }

    @Override
    public void checkIsSan() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(user != null) {
                    if (user.isSanctioned()) {
                        //deleteMyChatRoom();
                        matchPresenter.logout(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*private void deleteMyChatRoom() {
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    if(chatRoom != null) {
                        ChatRoom.Info info = chatRoom.getInfo();
                        Map<String, ChatRoom.Chat> chats = chatRoom.getChats();

                        Log.e(TAG, "test test test tes111111111");

                        if (info.getUser1Uid().equals(firebaseUser.getUid()) || info.getUser2Uid().equals(firebaseUser.getUid())) {    //현재 나와 채팅하고 있는 채팅방이라면

                            for (String key : chats.keySet()) {    //"chats" DB 돌리기
                                ChatRoom.Chat chat = chats.get(key);

                                if(chat != null && chat.getImageURL() != null) {
                                    deleteImage(chat.getImageURL());
                                }
                            }
                            Log.e(TAG, "test test test tes22222222222");
                            snapshot.getRef().removeValue();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void deleteImage(String imageURL) {
        String fileName = imageURL.substring(imageURL.indexOf("F")+1, imageURL.indexOf("?"));
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads").child(fileName);
        storageReference.delete();
    }

    private void calculateLeftTime(long madeTime, String chatRoomUid) {
        try {
            //현재 Date 구하기
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREAN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            //현재 Date를 시간 형태로 parsing 후 time 가져오기
            currentDate = dateFormat.parse(dateFormat.format(currentDate));
            long currentDateTime = currentDate.getTime();

            //초 단위로 표현
            long differenceSec = (currentDateTime - madeTime) / 1000;

            differenceSec -= TIME_LIMIT;  //제한시간과의 차이를 계산하기 위해 제한시간 만큼 빼기

            long shownMin = differenceSec / 60;
            long shownSec = differenceSec % 60;

            String shownMinStr = String.valueOf(shownMin);
            String shownSecStr = String.valueOf(shownSec);

            if(!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //시간이 다되어 0초 이상으로 넘어가게 된다면!
                deleteChatRoom(chatRoomUid);
            } else {
                Log.e(TAG, "howManyChat : you already chatting");
                matchPresenter.showSnackBar("이미 대화중인 상대가 있습니다.\n채팅방을 나가면 새로운 매칭을 할 수 있습니다.");
                matchPresenter.randomMatchBtnOff(); //매치 검색버튼 검색해제 상태로 만들기
                matchPresenter.randomMatchBtnEnable();
                if(isThreadRunning)
                    timeCheckThread.interrupt();
            }

        }
        catch (ParseException e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    private void deleteChatRoom(String chatRoomUid) {
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("info");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "howManyChat : you have deleted oldChatRoom");
                        requestMatch();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);
                    if(chat != null && chat.getImageURL() != null) {
                        deleteImage(chat.getImageURL());
                    }
                }
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}





















