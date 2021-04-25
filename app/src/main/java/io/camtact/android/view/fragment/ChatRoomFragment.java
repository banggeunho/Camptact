package io.camtact.android.view.fragment;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

import io.camtact.android.R;
import io.camtact.android.adapter.ChatRoomAdapter;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.mvp_interface.ChatRoomMVP;
import io.camtact.android.presenter.ChatRoomPresenter;

import static io.camtact.android.adapter.ChatRoomAdapter.aIsThreadRunning;

public class ChatRoomFragment extends Fragment implements ChatRoomMVP.View {

    private ChatRoomPresenter chatRoomPresenter;
    private static final String TAG = "ChatRoomFragment";

    private ArrayList<ChatRoom> chatRoomList;
    private RecyclerView recyclerView;
    private ChatRoomAdapter chatRoomAdapter;

    private ProgressBar progressCircle;
    private ImageView noFriendImg;
    private TextView noFriendText;

    @Override
    public void onCreate(Bundle savedInstatanceState) {
        super.onCreate(savedInstatanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v;

        setupMVP();

        if(getContext() != null && chatRoomPresenter.checkOnlineStatus(getContext())) {
             v = inflater.inflate(R.layout.fragment_chatroom, container, false);
            setupView(v);
            //initAd(v);
        } else {
            v = inflater.inflate(R.layout.fragment_offline, container, false);
        }

        return v;
    }

    private void setupMVP() {
        chatRoomPresenter = new ChatRoomPresenter(this);
    }

    private void setupView(View v) {

        progressCircle = v.findViewById(R.id.progressbar_circle);

        noFriendImg = v.findViewById(R.id.no_friend_image);
        noFriendText = v.findViewById(R.id.no_friend_text);

        noFriendImg.setVisibility(View.GONE);
        noFriendText.setVisibility(View.GONE);

        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatRoomList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(getContext(), chatRoomList);
    }

    @Keep
    private void initAd(View v) {
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void addChatRoom(ChatRoom chatRoom) {
        chatRoomAdapter.addItem(chatRoom);
    }

    @Override
    public void removeAll() {
        chatRoomAdapter.removeAll();
    }

    @Override
    public void getRoomCount() {
        progressCircle.setVisibility(View.GONE);
       if(chatRoomAdapter.getItemCount() == 0) {
          // noFriendImg.setVisibility(View.VISIBLE);
           noFriendText.setVisibility(View.VISIBLE);
       }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(chatRoomPresenter.checkOnlineStatus(getContext())) {
            recyclerView.setAdapter(chatRoomAdapter);
            //액티비티에 들렸다오면 onCreateView()가 호출이 안되서 스레드 발동이 안되므로 onResume에 setAdapter로 스레드 발동시켜줌
            chatRoomPresenter.addChatRoomList();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        aIsThreadRunning = false;
        chatRoomPresenter.removeListener();
    }
}















