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

    // Adapter의 특정 위치(playPosition)에 있는 데이터를 보여줘야 할때 호출됩니다.
    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder viewHolder, final int position) {

        //스크롤이 움직일 때마다 변경됨. 새로 보여줘야하는 값이 position
        ChatRoom.Chat chat = chatList.get(position);

        if(chat.getMessage() != null) { //메세지가 null이 아니라면 즉, 메시지라면
            viewHolder.messageText.setText(chat.getMessage());
        } else { //메세지가 null이라면 즉, 이미지라면 이미지 로딩

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
                //클립보드 사용 코드
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", viewHolder.messageText.getText()); //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                clipboardManager.setPrimaryClip(clipData);

                //복사가 되었다면 토스트메시지 노출
                Snackbar snackbar = Snackbar.make(viewHolder.messageText, "복사되었습니다.", 1500);
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

        long madeTime = Long.parseLong(String.valueOf(chat.getTime())); //Object를 long으로 파싱
        Date date = new Date(madeTime);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = simpleDateFormat.format(date);

        if(!chat.isNotice()) {    //시간이 비워지지않았다면, 즉 노티스msg가 아니라면

            if (position != chatList.size() - 1) { //마지막 말풍선이 아니라면
                ChatRoom.Chat nextChat = chatList.get(position + 1);

                long nextMadeTime = Long.parseLong(String.valueOf(nextChat.getTime())); //Object를 long으로 파싱
                Date nextDate = new Date(nextMadeTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String nextChatTime = simpleDateFormat.format(nextDate);

                if (!time.equals(nextChatTime)) {   //다음 말풍선과 시간이 다르다면
                    viewHolder.timeText.setVisibility(View.VISIBLE);
                    viewHolder.timeText.setText(time);
                } else {    //다음 말풍선과 시간이 같다면 시간text 숨김
                    viewHolder.timeText.setVisibility(View.INVISIBLE);
                }
            } else {    //마지막 말풍선이라면 그냥 시간 보여주기
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

            if (chatList.get(position).getMessage() != null) {  //메세지가 null이 아니라면 즉, 메시지라면
                if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
                    return MSG_TYPE_RIGHT;
                } else {
                    return MSG_TYPE_LEFT;
                }
            } else {    //메세지가 null이라면 즉, 이미지라면
                if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
                    return IMG_TYPE_RIGHT;
                } else {
                    return IMG_TYPE_LEFT;
                }
            }
        } else {    //메시지의 time이 null이라면 = notice 메세지
            return MSG_TYPE_NOTICE;
        }

    }
}

