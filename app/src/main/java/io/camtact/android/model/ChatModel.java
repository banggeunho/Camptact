package io.camtact.android.model;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.model.dto.User;
import io.camtact.android.mvp_interface.ChatMVP;

import static io.camtact.android.presenter.ChatPresenter.uploadTask;

public class ChatModel implements ChatMVP.Model {

    private ChatMVP.Presenter chatPresenter;

    private static final String TAG = "ChatModel";
    private String chatRoomUid;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference reference;
    private DatabaseReference chatsRef;
    private ChildEventListener seenListener;
    private ValueEventListener readListener;

    private StorageReference storageReference;

    private String friendUid;

    public ChatModel(ChatMVP.Presenter presenter) {
        this.chatPresenter = presenter;
    }


    @Override
    public void applyFriendNameAndReadMsg(final String friendUid) {

        this.friendUid = friendUid;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(friendUid);
        //refer friendName on Database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                //set friendName on the top of the Activity

                if(user != null)
                chatPresenter.setFriendNameText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void checkChatRoom() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    if (chatRoom != null) {
                        ChatRoom.Info info = chatRoom.getInfo();

                        if (info != null && info.getUser1Uid().equals(firebaseUser.getUid()) && info.getUser2Uid().equals(friendUid)
                                || info != null && info.getUser1Uid().equals(friendUid) && info.getUser2Uid().equals(firebaseUser.getUid())) {    //현재 이 채팅하고 채팅방 Uid 찾기

                            chatRoomUid = snapshot.getKey();
                            if (readListener == null) {
                                readMessage();
                            }
                            if (seenListener == null) {
                                seenMessage();
                            }
                            
                            showLeftTime();
                            break;
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
    public void seenMessage() {
        if(chatRoomUid == null) {
            checkChatRoom();
        } else {
            if(seenListener != null) return;

            chatsRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
            seenListener = chatsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //데이터가 삭제됬을 때는 관여안하기 위해 onChildAdded 에만 발동.

                    ChatRoom.Chat chat = dataSnapshot.getValue(ChatRoom.Chat.class);
                    if (chat != null && !chat.isIsSeen() && !chat.getSender().equals(firebaseUser.getUid())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        //update "isSeen" value 'true' in "Chats" DB
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void removeWholeListener() {
        if(seenListener != null) {
            chatsRef.removeEventListener(seenListener);
            seenListener = null;
        }
        if(readListener != null) {
            chatsRef.removeEventListener(readListener);
            readListener = null;
        }
    }

    @Override
    public void sendMessage(String sender, String message) {
        if (chatRoomUid == null) {
            chatPresenter.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 해당 현상이 계속될 경우 재접속 해주시기 바랍니다.", 3500, true);
            checkChatRoom();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("message", message);
            hashMap.put("isSeen", false);
            hashMap.put("time", ServerValue.TIMESTAMP);
            hashMap.put("imageURL", null);
            hashMap.put("notice", false);
            //push hashMap(Message data set) into Firebase Database.
            reference.push().setValue(hashMap);
        }


        //메세지 보낼때 자기가 보낸거면(오른쪽말풍선이면) 스크롤 내리기
        /*if(sender.equals(firebaseUser.getUid())) {
            chatView.scrollDown();
        }*/
    }

    @Override
    public void uploadImage(final Activity activity, Uri imageUri, final File toDeleteFile) {
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + chatPresenter.getFileExtension(imageUri, activity));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if(downloadUri != null) {
                            String mUri = downloadUri.toString();
                            sendImage(firebaseUser.getUid(), mUri);
                        }
                        chatPresenter.hideProgressBar();
                    } else {
                        Log.e("ChatModel", "Failed");
                        chatPresenter.hideProgressBar();
                    }
                    //chatPresenter.deleteFileFromMediaStore(activity.getContentResolver(), toDeleteFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ChatModel", "" + e.getMessage());
                    chatPresenter.hideProgressBar();
                }
            });
        } else {
            Log.e("ChatModel", "No image selected");
        }
    }

    private void sendImage(String sender, String mUri) {
        if (chatRoomUid == null) {
            chatPresenter.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 해당 현상이 계속될 경우 재접속 해주시기 바랍니다.", 3500, true);
            checkChatRoom();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("message", null);
            hashMap.put("isSeen", false);
            hashMap.put("time", ServerValue.TIMESTAMP);
            hashMap.put("imageURL", mUri);
            hashMap.put("notice", false);
            reference.push().setValue(hashMap);
        }


        //메세지 보낼때 자기가 보낸거면(오른쪽말풍선이면) 스크롤 내리기
        /*if(sender.equals(firebaseUser.getUid())) {
            chatView.scrollDown();
        }*/
    }


    @Override
    public void readMessage() {
        if(chatRoomUid == null) {
            checkChatRoom();
        } else {
            if(readListener != null) return;

            chatsRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
            readListener = chatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int chatEA = 0;
                    chatPresenter.removeAllMsg();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);

                        if(chat != null) {
                            chatEA++;
                            chatPresenter.addMsg(chat);
                        }
                    }
                    if (chatEA == 0) { //채팅 갯수가 0개면 상대방쪽에서 삭제한 것(방폭)이므로
                        //상대가 탈퇴인지 사용제재인지 알아보기.

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final User me = dataSnapshot.getValue(User.class);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(friendUid);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        int reason = 0;
                                        User friend = dataSnapshot.getValue(User.class);
                                        if (friend == null) {    //회원탈퇴한 유저
                                            reason = 1;
                                        } else if (friend.isSanctioned()) {
                                            reason = 2;
                                        } else if (me != null && me.isGetKicked()) {
                                            reason = 3;
                                        }
                                        chatPresenter.finishActivity(reason);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        removeWholeListener();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void showLeftTime() {
        if(chatRoomUid == null) {
            checkChatRoom();
        } else {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("info");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoom.Info info = dataSnapshot.getValue(ChatRoom.Info.class);

                    if(info != null) {
                        long madeTime = Long.parseLong(String.valueOf(info.getMadeTime())); //Object를 long으로 파싱
                        chatPresenter.runTimeChecker(madeTime);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void report(final String reason) {
        if (chatRoomUid == null) {
            chatPresenter.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 해당 현상이 계속될 경우 재접속 해주시기 바랍니다.", 3500, true);
            checkChatRoom();
        } else {
            //신고받는 DB
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TroubleReports").child(firebaseUser.getUid()).child(reason).push();
            //상대방과 한 채팅내용 전부 신고 DB에 업로드
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);

                        if(chat != null) {
                            reference.push().setValue(chat);
                        }
                    }
                    chatPresenter.hideProgressBar();
                    chatPresenter.showSnackBar(reason + "을(를) 이유로 상대방을 신고 하였습니다", 3000, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }


    @Override
    public void deleteChatRoom(final boolean myself) {
        if(chatRoomUid == null) {
            Log.e(TAG, "chatRoomUid is null");
            chatPresenter.showSnackBar("네트워크 연결 상태가 좋지 않습니다. 해당 현상이 계속될 경우 재접속 해주시기 바랍니다.", 3500, true);
            checkChatRoom();
        } else {
            if (myself && friendUid != null) {    //내가 채팅방을 지운거니 상대 Users 디비에 지운 상태 업뎃
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(friendUid);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("getKicked", true);

                reference.updateChildren(hashMap);
            }

            reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("info");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);
                        if (chat != null && chat.getImageURL() != null) {
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

    private void deleteImage(String imageURL) {
        String fileName = imageURL.substring(imageURL.indexOf("F")+1, imageURL.indexOf("?"));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads").child(fileName);
        storageReference.delete();
    }

}
