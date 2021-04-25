package io.camtact.android.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import io.camtact.android.R;
import io.camtact.android.mvp_interface.MatchMVP;
import io.camtact.android.presenter.MatchPresenter;
import io.camtact.android.view.AuthActivity;

import static io.camtact.android.presenter.MatchPresenter.isThreadRunning;
import static io.camtact.android.presenter.MatchPresenter.timeCheckThread;

public class MatchFragment extends Fragment implements MatchMVP.View {

    private static final String TAG = "MatchFragment";

    private MatchPresenter matchPresenter;

    private ToggleButton randomMatchBtn;
    private ProgressBar progressBar, progressCircle;
    private TextView searchingText, noticeText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v;

        setupMVP();

        if(getContext() != null && matchPresenter.checkOnlineStatus(getContext())) {
            v = inflater.inflate(R.layout.fragment_match, container, false);
            setupView(v);
            matchPresenter.isSearching();
            initAd(v);
        } else {
            v = inflater.inflate(R.layout.fragment_offline, container, false);
        }

        return v;
    }

    private void setupMVP() {
        matchPresenter = new MatchPresenter(this);
    }

    private void setupView(View v) {

        progressBar = v.findViewById(R.id.progressbar);
        progressCircle = v.findViewById(R.id.progressbar_circle);
        searchingText = v.findViewById(R.id.searching_text);
        noticeText = v.findViewById(R.id.notice_text2);

        progressBar.setVisibility(View.INVISIBLE);
        progressCircle.setVisibility(View.INVISIBLE);
        searchingText.setVisibility(View.INVISIBLE);
        noticeText.setVisibility(View.INVISIBLE);

        randomMatchBtn = v.findViewById(R.id.random_match_btn);
        randomMatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getContext() != null && matchPresenter.checkOnlineStatus(getContext())) {
                    if(randomMatchBtn.isChecked()) {
                        matchPresenter.searchRandomUser();
                        progressBar.setVisibility(View.VISIBLE);

                    } else {
                        matchPresenter.stopMatch();
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.");
                }
            }
        });
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
    public void showSnackBar(String msg) {
        if(getActivity()==null) return;
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), msg, 2500);
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

    @Override
    public void randomMatchBtnOff() {
        randomMatchBtn.setChecked(false);
    }

    @Override
    public void randomMatchBtnDisable() {
        randomMatchBtn.setEnabled(false);
    }

    @Override
    public void randomMatchBtnEnable() {
        randomMatchBtn.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showProgressCircle() {
        progressCircle.setVisibility(View.VISIBLE);
        searchingText.setVisibility(View.VISIBLE);
        noticeText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressCircle() {
        progressCircle.setVisibility(View.INVISIBLE);
        searchingText.setVisibility(View.INVISIBLE);
        noticeText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goAuthActivity(boolean isSanctioned) {
        Intent intent = new Intent(getContext(), AuthActivity.class);
        intent.putExtra("isSanctioned", isSanctioned);
        startActivity(intent);
        if(getActivity() != null)
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        matchPresenter.checkIsSan();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(isThreadRunning) {
            timeCheckThread.interrupt();
        }
    }
}


















