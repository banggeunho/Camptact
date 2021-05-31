package io.camtact.android.view.fragment;

import android.os.Bundle;
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

public class GroupChatFragment extends Fragment implements ChatRoomMVP.View {


    private ChatRoomPresenter chatRoomPresenter;
    private static final String TAG = "GroupChatFragment";

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
        v = inflater.inflate(R.layout.fragment_groupchat, container, false);
        setupView(v);
        initAd(v);
        return v;
    }


    private void setupView(View v) {

       // progressCircle = v.findViewById(R.id.progressbar_circle);
        noFriendText = v.findViewById(R.id.no_friend_text);
        noFriendText.setVisibility(View.GONE);
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

    }
    @Override
    public void removeAll() {
    }
    @Override
    public void getRoomCount() {
    }


}
