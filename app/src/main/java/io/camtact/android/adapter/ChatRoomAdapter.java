package io.camtact.android.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.camtact.android.R;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.view.ChatActivity;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static io.camtact.android.presenter.ChatPresenter.TIME_LIMIT;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private static final String TAG = "ChatRoomAdapter";

    private Context context;
    private List<ChatRoom> chatRoomList;
    private String chatRoomUid;
    private String theLastMessage;
    private String shownMinStr, shownSecStr;
    private int unread;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private Thread aTimeCheckThread;
    public static boolean aIsThreadRunning = false;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private CardView cardView;
        private TextView lastMsg;
        private TextView unreadMsgEA;
        private TextView timeLimit;
        private ImageButton closeBtn;

        public ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.username);
            cardView = view.findViewById(R.id.cardview);
            lastMsg = view.findViewById(R.id.last_msg);
            unreadMsgEA = view.findViewById(R.id.unread_msg);
            timeLimit = view.findViewById(R.id.time_limit);
            closeBtn = view.findViewById(R.id.close_btn);
        }
    }

    public ChatRoomAdapter(Context context, List<ChatRoom> list) {
        this.context = context;
        this.chatRoomList = list;
    }

    // RecyclerView??? ????????? ???????????? ???????????? ?????? ????????? ViewHolder??? ???????????? ??? ??? ???????????????.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_room_item, viewGroup, false);

        return new ChatRoomAdapter.ViewHolder(view);
    }

    // Adapter??? ?????? ??????(playPosition)??? ?????? ???????????? ???????????? ?????? ???????????????.
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewholder, final int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        //?????? user1?????????
        if(chatRoom.getInfo().getUser1Uid().equals(firebaseUser.getUid()) ) {

            checkRoomUid(chatRoom.getInfo().getUser2Uid(), viewholder.timeLimit, viewholder.unreadMsgEA, viewholder.lastMsg, viewholder.closeBtn);

            viewholder.userName.setText(chatRoom.getInfo().getUser2Name());
            viewholder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to ChatActivity with touched chat_room_item info
                    if(checkOnlineStatus()) {
                        if (!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //(?????????????????? ????????????)
                            Log.e(TAG, "time over");
                        } else {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("uid", chatRoomList.get(position).getInfo().getUser2Uid());
                            context.startActivity(intent);
                        }
                    } else {
                        showSnackBar(viewholder.cardView, "???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.");
                    }
                }
            });

        } else {    //?????? user2??????

            checkRoomUid(chatRoom.getInfo().getUser1Uid(), viewholder.timeLimit, viewholder.unreadMsgEA, viewholder.lastMsg, viewholder.closeBtn);

            viewholder.userName.setText(chatRoom.getInfo().getUser1Name());
            viewholder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //go to ChatActivity with touched chat_room_item info
                    if(checkOnlineStatus()) {
                        if (!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //(?????????????????? ????????????)
                            Log.e(TAG, "time over");
                        } else {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("uid", chatRoomList.get(position).getInfo().getUser1Uid());
                            context.startActivity(intent);
                        }
                    } else {
                        showSnackBar(viewholder.cardView, "???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.");
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return (null != chatRoomList ? chatRoomList.size() : 0);
    }

    public void removeItem(int position) {
        chatRoomList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, chatRoomList.size());
    }

    public void removeAll() {
        chatRoomList.clear();
        notifyDataSetChanged();
    }

    public void addItem(int position, ChatRoom chatRoom) {
        chatRoomList.add(position, chatRoom);
        notifyDataSetChanged();
    }

    public void addItem(ChatRoom chatRoom) {
        chatRoomList.add(chatRoom);
        notifyDataSetChanged();
    }

    private void showSnackBar(View view, String msg) {
        Snackbar snackbar = Snackbar.make(view, msg, 3000);
        View snackBarLayout = snackbar.getView();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Layout must match parent layout type
        lp.setMargins(0, 300, 0, 0);
        // Margins relative to the parent view.
        snackBarLayout.setLayoutParams(lp);
        snackbar.show();
    }

    private void checkRoomUid(final String friendUid, final TextView timeLimit, final TextView unreadMsgEA, final TextView lastMsg, final ImageButton closeBtn) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    if(chatRoom != null) {
                        ChatRoom.Info info = chatRoom.getInfo();

                        if (info != null && info.getUser1Uid().equals(firebaseUser.getUid()) && info.getUser2Uid().equals(friendUid)
                                || info != null && info.getUser1Uid().equals(friendUid) && info.getUser2Uid().equals(firebaseUser.getUid())) {    //?????? ??? ???????????? ????????? Uid ??????

                            chatRoomUid = snapshot.getKey();

                            runTimeChecker(Long.parseLong(String.valueOf(info.getMadeTime())), timeLimit, chatRoomUid);
                            unreadMsg(unreadMsgEA);
                            showLastMessage(lastMsg);

                            closeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(checkOnlineStatus()) {
                                        if (!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //(?????????????????? ????????????)
                                            Log.e(TAG, "time over");
                                        } else {
                                            showExitDialog(chatRoomUid, friendUid);
                                        }
                                    } else {
                                        showSnackBar(closeBtn, "???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.");
                                    }
                                }
                            });

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

    private void showExitDialog(final String chatRoomUid, final String friendUid) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("???????????? ???????????? ?????? ????????? ??? ????????????. ?????? ?????????????????????????");
        dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteChatRoom(chatRoomUid, true, friendUid);
            }
        });
        dialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    private void unreadMsg(final TextView unreadMsgEA) {
        unread = 0;
        DatabaseReference unreadMsgRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");
        unreadMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);

                    if(chat != null && chat.getSender() != null) {
                        if (!chat.isIsSeen() && !chat.getSender().equals(firebaseUser.getUid())) {
                            unread++;
                        }
                    }
                }

                String unreadMsgStr = Integer.toString(unread);
                if(unread==0) {
                    unreadMsgEA.setVisibility(View.INVISIBLE);
                } else {
                    unreadMsgEA.setVisibility(View.VISIBLE);
                    unreadMsgEA.setText(unreadMsgStr);
                }
                unread = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showLastMessage(final TextView lastMsg) {
        theLastMessage = "default";
        DatabaseReference lastMsgRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomUid).child("chats");

        lastMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom.Chat chat = snapshot.getValue(ChatRoom.Chat.class);
                    if (firebaseUser != null && chat != null && chat.getSender() != null) {
                        if (chat.getMessage() != null) { //???????????????
                            theLastMessage = chat.getMessage();
                        } else {    //???????????????
                            theLastMessage = "????????? ???????????????";
                        }
                    }

                }
                switch (theLastMessage){
                    case  "default":
                        lastMsg.setVisibility(View.INVISIBLE);
                        break;

                    default:
                        lastMsg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void runTimeChecker(final long madeTime, final TextView timeLimit, final String chatRoomUid) {

        final Handler handler = new Handler();

        aTimeCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                aIsThreadRunning = true;
                while (aIsThreadRunning) {
                        final String aLeftTime = calculateLeftTime(madeTime, chatRoomUid);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                timeLimit.setText(aLeftTime);
                            }
                        });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException" + e.getMessage());
                        aIsThreadRunning = false;
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        aTimeCheckThread.setDaemon(true);
        aTimeCheckThread.start();
    }

    private String calculateLeftTime(long madeTime, String chatRoomUid) {

        //?????? Date ?????????
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREAN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        try {
            //?????? Date??? ?????? ????????? parsing ??? time ????????????
            currentDate = dateFormat.parse(dateFormat.format(currentDate));
            long currentDateTime = currentDate.getTime();

            synchronized (this) {
                //??? ????????? ??????
                long differenceSec = (currentDateTime - madeTime) / 1000;

                differenceSec -= TIME_LIMIT;  //30????????? ????????? ???????????? ?????? 30???(1800???) ??????

                long shownMin = differenceSec / 60;
                long shownSec = differenceSec % 60;

                shownMinStr = String.valueOf(shownMin);
                shownSecStr = String.valueOf(shownSec);

                if (!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //????????? ????????? 0??? ???????????? ???????????? ?????????!
                    Thread.currentThread().interrupt();
                    deleteChatRoom(chatRoomUid, false, null);
                }

                if (shownSecStr.length() == 2 || shownSecStr.equals("0")) {   // ??????????????? ???????????? ??????????????? ?????? 2??? ???????????? ??? ?????? ???
                    shownSecStr = "0" + shownSecStr;
                }
            }

            String calLeftTime = shownMinStr + ":" + shownSecStr;

            return calLeftTime.replaceAll("-", ""); //-?????? ???????????????
        } catch (ParseException e) {
            Log.e(TAG, "" + e.getMessage());

            return "error";
        }
    }

    private void deleteChatRoom(String chatRoomUid, boolean myself, String friendUid) {
        if(myself && friendUid != null) {    //?????? ???????????? ???????????? ??? Users ????????? ?????? ?????? ??????
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

    private void deleteImage(String imageURL) {
        String fileName = imageURL.substring(imageURL.indexOf("F")+1, imageURL.indexOf("?"));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads").child(fileName);
        storageReference.delete();
    }

    private boolean checkOnlineStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

}
