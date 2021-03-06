package io.camtact.android.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.camtact.android.R;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.view.ImageActivity;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int IMG_TYPE_LEFT = 2;
    private static final int IMG_TYPE_RIGHT = 3;
    private static final int MSG_TYPE_NOTICE = 4;

    private Context context;
    private List<ChatRoom.Chat> chatList;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREAN);

    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<ChatRoom.Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private ImageView imageView;
        private ImageView seenView;
        private TextView timeText;

        public ViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.msg_text);
            imageView = view.findViewById(R.id.image_view);
            seenView = view.findViewById(R.id.seen_img);
            timeText = view.findViewById(R.id.time_text);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, viewGroup, false);
            return new ViewHolder(view);
        } else if(viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, viewGroup, false);
            return new ViewHolder(view);
        } else if(viewType == IMG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.img_item_right, viewGroup, false);
            return new ViewHolder(view);
        } else if(viewType == IMG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.img_item_left, viewGroup, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_notice, viewGroup, false);
            return new ViewHolder(view);
        }
    }

    // Adapter??? ?????? ??????(playPosition)??? ?????? ???????????? ???????????? ?????? ???????????????.
    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder viewHolder, final int position) {

        //???????????? ????????? ????????? ?????????. ?????? ?????????????????? ?????? position
        ChatRoom.Chat chat = chatList.get(position);

        if(chat.getMessage() != null) { //???????????? null??? ???????????? ???, ???????????????
            viewHolder.messageText.setText(chat.getMessage());
        } else { //???????????? null????????? ???, ??????????????? ????????? ??????

            RequestOptions sharedOptions = new RequestOptions()
                            .placeholder(R.drawable.loading_image)
                            .error(R.drawable.fail_image)
                            .priority(Priority.HIGH)
                            .fitCenter();

            Glide.with(context)
                    .load(chat.getImageURL())
                    .apply(sharedOptions)
                    .into(viewHolder.imageView);
        }

        viewHolder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //???????????? ?????? ??????
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", viewHolder.messageText.getText()); //??????????????? ID?????? ???????????? id ?????? ???????????? ??????
                clipboardManager.setPrimaryClip(clipData);

                //????????? ???????????? ?????????????????? ??????
                Snackbar snackbar = Snackbar.make(viewHolder.messageText, "?????????????????????.", 1500);
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

                return false;
            }
        });

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("imageURL", chatList.get(position).getImageURL());
                context.startActivity(intent);
            }
        });

        long madeTime = Long.parseLong(String.valueOf(chat.getTime())); //Object??? long?????? ??????
        Date date = new Date(madeTime);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = simpleDateFormat.format(date);

        if(!chat.isNotice()) {    //????????? ????????????????????????, ??? ?????????msg??? ????????????

            if (position != chatList.size() - 1) { //????????? ???????????? ????????????
                ChatRoom.Chat nextChat = chatList.get(position + 1);

                long nextMadeTime = Long.parseLong(String.valueOf(nextChat.getTime())); //Object??? long?????? ??????
                Date nextDate = new Date(nextMadeTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String nextChatTime = simpleDateFormat.format(nextDate);

                if (!time.equals(nextChatTime)) {   //?????? ???????????? ????????? ????????????
                    viewHolder.timeText.setVisibility(View.VISIBLE);
                    viewHolder.timeText.setText(time);
                } else {    //?????? ???????????? ????????? ????????? ??????text ??????
                    viewHolder.timeText.setVisibility(View.INVISIBLE);
                }
            } else {    //????????? ?????????????????? ?????? ?????? ????????????
                viewHolder.timeText.setVisibility(View.VISIBLE);
                viewHolder.timeText.setText(time);
            }
        }


        if (chat.isIsSeen()) {
            viewHolder.seenView.setVisibility(View.GONE);
        } else {
            viewHolder.seenView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return (null != chatList ? chatList.size() : 0);
    }

    public void removeItem(int position) {
        chatList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, chatList.size());
    }

    public void removeAll() {
        chatList.clear();
        notifyDataSetChanged();
    }

    public void addItem(ChatRoom.Chat chat) {
        chatList.add(chat);
        notifyDataSetChanged();
    }

    public void editItem(int position, ChatRoom.Chat chat) {
        chatList.set(position, chat);
        notifyItemChanged(position);
    }

    //set msg position whether right side or left.
    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (!chatList.get(position).isNotice()) {

            if (chatList.get(position).getMessage() != null) {  //???????????? null??? ???????????? ???, ???????????????
                if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
                    return MSG_TYPE_RIGHT;
                } else {
                    return MSG_TYPE_LEFT;
                }
            } else {    //???????????? null????????? ???, ???????????????
                if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
                    return IMG_TYPE_RIGHT;
                } else {
                    return IMG_TYPE_LEFT;
                }
            }
        } else {    //???????????? time??? null????????? = notice ?????????
            return MSG_TYPE_NOTICE;
        }

    }
}

