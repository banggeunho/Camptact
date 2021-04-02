package io.camtact.android.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.camtact.android.model.dto.User;
import io.camtact.android.mvp_interface.SetMVP;

public class SetModel implements SetMVP.Model {

    private SetMVP.Presenter setPresenter;

    private static final String TAG = "SetModel";

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    public SetModel(SetMVP.Presenter presenter) {
        this.setPresenter = presenter;
    }

    @Override
    public void getUserName() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null && user.getUserName() == null) {
                    setPresenter.logout(false);
                } else if(user != null) {
                    setPresenter.setUserName(user.getUserName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void editUserName(final String userName) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userName", userName);
        reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setPresenter.hideProgressBar();
                setPresenter.setUserName(userName);
            }
        });
    }

    @Override
    public void outOfMembership() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //delete all user's DB before leave member
                                //deleteAllDB();
                                setPresenter.hideProgressBar();
                                setPresenter.logout(true);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setPresenter.hideProgressBar();
                    setPresenter.showLogoutDialog();
                }
            });
        }
    }

    /*private void deleteAllDB() {
        //deleteChatRoom();
        //last delete 'User' DB
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "dataSnapshot.getValue() :" + dataSnapshot.getValue());
                dataSnapshot.getRef().removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    /*private void deleteChatRoom() {
        reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    if(chatRoom != null) {
                        ChatRoom.Info info = chatRoom.getInfo();
                        Map<String, ChatRoom.Chat> chats = chatRoom.getChats();

                        if (info.getUser1Uid().equals(firebaseUser.getUid()) || info.getUser2Uid().equals(firebaseUser.getUid())) {    //현재 나와 채팅하고 있는 채팅방이라면

                            for (String key : chats.keySet()) {    //"chats" DB 돌리기
                                ChatRoom.Chat chat = chats.get(key);
                                if (chat != null && chat.getImageURL() != null) {
                                    deleteImage(chat.getImageURL());
                                }
                            }
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

    /*private void deleteImage(String imageURL) {
        String fileName = imageURL.substring(imageURL.indexOf("F")+1, imageURL.indexOf("?"));
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads").child(fileName);
        storageReference.delete();
    }*/

    @Override
    public void sendInquiry(String inquiry) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Inquiries").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", ServerValue.TIMESTAMP);
        hashMap.put("content", inquiry);
        reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setPresenter.hideProgressBar();
                setPresenter.showSnackBar("문의 및 건의가 정상적으로 접수되었습니다 \n 가입하신 메일로 빠른 시일 내에 답변드리겠습니다", 2500, true);
            }
        });
    }

    @Override
    public void reportError(String error) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ErrorReports").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", ServerValue.TIMESTAMP);
        hashMap.put("content", error);
        reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setPresenter.hideProgressBar();
                setPresenter.showSnackBar("에러가 정상적으로 접수되었습니다", 2500, true);
            }
        });
    }
}


















