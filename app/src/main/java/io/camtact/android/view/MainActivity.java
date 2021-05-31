package io.camtact.android.view;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import io.camtact.android.R;
import io.camtact.android.view.fragment.BoardFragment;
import io.camtact.android.view.fragment.ChatRoomFragment;
import io.camtact.android.view.fragment.MatchFragment;
import io.camtact.android.view.fragment.SetFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 4개의 메뉴에 들어갈 Fragment들
    private MatchFragment matchFragment = new MatchFragment();
    private ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
    private SetFragment setFragment = new SetFragment();
    private BoardFragment boardFragment = new BoardFragment();

    private long backPressedTime= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        int reason = getIntent().getIntExtra("reasonForFinish", -1);
        if (reason == 0) {
            showSnackBar("시간이 만료되어 채팅이 종료되었습니다.", 2000, true);
        } else if (reason == 1) {
            showSnackBar("상대방이 회원 탈퇴하여 채팅이 종료되었습니다.", 3000, true);
        } else if (reason == 2) {
            showSnackBar("상대방의 계정이 정지당하여 채팅이 종료되었습니다.", 3000, true);
        } else if (reason == 3) {
            showSnackBar("상대방이 채팅방을 삭제하여 채팅이 종료되었습니다.", 2000, true);
        }

        // 첫 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, matchFragment).commitAllowingStateLoss();

        // bottomNavigationView의 아이템이 선택될 때 호출될 리스너 등록
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.navigation_match: {
                        transaction.replace(R.id.frame_layout, matchFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_chat: {
                        transaction.replace(R.id.frame_layout, chatRoomFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_brd: {
                        transaction.replace(R.id.frame_layout, boardFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_set: {
                        transaction.replace(R.id.frame_layout, setFragment).commitAllowingStateLoss();
                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showSnackBar(String message, int milliTime, boolean onTheTop) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, milliTime);
        if(onTheTop) {  //if the position of Snackbar is on the top
            View snackBarLayout = snackbar.getView();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Layout must match parent layout type
            lp.setMargins(0, 300, 0, 0);
            // Margins relative to the parent view.
            snackBarLayout.setLayoutParams(lp);
        }
        snackbar.show();
    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-backPressedTime>=2000){
            backPressedTime=System.currentTimeMillis();
            showSnackBar("뒤로가기 버튼을 한번 더 누르면 종료됩니다", 2000,false);
        }else if(System.currentTimeMillis()-backPressedTime<2000){
            finish();
        }
    }
}
