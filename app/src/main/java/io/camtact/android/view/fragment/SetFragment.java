package io.camtact.android.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import io.camtact.android.R;
import io.camtact.android.mvp_interface.SetMVP;
import io.camtact.android.presenter.SetPresenter;
import io.camtact.android.view.AuthActivity;

public class SetFragment extends Fragment implements SetMVP.View {

    private SetPresenter setPresenter;

    private Button askBtn, errorReportBtn, leaveBtn;
    private ImageButton editNameImgBtn;
    private TextView editNameText, contactText;
    private RelativeLayout editNameLayout;
    private ProgressBar progressBar;

    private InputMethodManager imm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v;

        setupMVP();

        if(getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
            v = inflater.inflate(R.layout.fragment_set, container, false);
            setupView(v);
            setPresenter.getUserName();
            initAd(v);
        } else {
            v = inflater.inflate(R.layout.fragment_offline, container, false);
        }

        return v;
    }

    private void setupMVP() {
        setPresenter = new SetPresenter(this);
    }

    private void setupView(View v) {
        editNameLayout = v.findViewById(R.id.edit_name_layout);
        editNameText = v.findViewById(R.id.edit_name_text);
        editNameImgBtn = v.findViewById(R.id.edit_name_img_btn);
        askBtn = v.findViewById(R.id.ask_btn);
        errorReportBtn = v.findViewById(R.id.error_report_btn);
        leaveBtn = v.findViewById(R.id.leave_btn);
        contactText = v.findViewById(R.id.contact_text);
        progressBar = v.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        if(getContext() != null) {
            imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        editNameImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        editNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        askBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAskDialog();
            }
        });

        errorReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog();
            }
        });

        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        contactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                // email setting 배열로 해놔서 복수 발송 가능
                String[] address = {"eskapizmy@gmail.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT,"camtact Chat 유저가 보냅니다.");
                if(firebaseUser != null) {
                    email.putExtra(Intent.EXTRA_TEXT, "발신자 uid : " + firebaseUser.getUid() + "\n");
                }
                startActivity(email);
            }
        });

    }

    private void initAd(View v) {
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("9425B3D2A5C734031F36632A80F10B1F")
                .addTestDevice("B518ED7493EE60C3ED642113D7A099BC")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void showEditDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("닉네임 변경하기");
            dialog.setMessage("선정적인 닉네임은 서비스 이용에 제한이 있을 수 있습니다.");
            // EditText 삽입하기
            final EditText editText = new EditText(getContext());
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            dialog.setView(editText);
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userName = editText.getText().toString();
                    if (!Pattern.matches("^[0-9a-zA-Z가-힣]*$", userName)) {
                        showSnackBar("숫자, 영문, 한글 이외의 문자(자음,모음 공백 포함)는 쓸 수 없습니다. ", 3000, true);
                    } else if (userName.length() < 2 || userName.length() > 12) {
                        showSnackBar("글자 수는 2~12자 이어야 합니다.", 3000, true);
                    } else {
                        if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                            setPresenter.editUserName(userName);
                            progressBar.setVisibility(View.VISIBLE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        } else {
                            showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    }

                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });
            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showAskDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("건의 및 문의                                          ");
            // EditText 삽입하기
            final EditText editText = new EditText(getContext());
            editText.setMaxLines(7);
            dialog.setView(editText);
            dialog.setPositiveButton("전송", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.sendInquiry(editText.getText().toString());
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true);
                    }

                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });

            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showReportDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("오류 알리기");
            dialog.setMessage("어느 '화면'의 어떤 '상황'에서 '어떻게' 오류가 발생하는지 자세히 설명해주시면 감사하겠습니다!");
            // EditText 삽입하기
            final EditText editText = new EditText(getContext());
            editText.setMaxLines(7);
            dialog.setView(editText);
            dialog.setPositiveButton("전송", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null &&  setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.sendError(editText.getText().toString());
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true);
                    }
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });

            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showAlertDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setMessage("탈퇴를 하게 되면 모든 정보가 삭제됩니다. \n 정말 탈퇴하시겠습니까?");
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        progressBar.setVisibility(View.VISIBLE);
                        setPresenter.outOfMembership();
                    } else {
                        showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true);
                    }
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }

    @Override
    public void showLogoutDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setMessage("계정 삭제와 같이 보안에 민감한 작업을 하려면 사용자가 최근에 로그인한 적이 있어야 합니다.\n" +
                    "로그인한지 오랜시간이 지났습니다. 탈퇴를 위해 로그아웃 후 재로그인 해주시기 바랍니다.");
            dialog.setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.logout(false);
                    } else {
                        showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true);
                    }
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean onTheTop) {
        if(getActivity() != null) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, milliTime);
            if (onTheTop) {  //if the position of Snackbar is on the top
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
    }

    @Override
    public void setUserName(String userName) {
        editNameText.setText("내 닉네임 : "+userName);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void goAuthActivity(boolean isLeaving) {
        Intent intent = new Intent(getContext(), AuthActivity.class);
        if(isLeaving) {
            intent.putExtra("leave", true);
        }
        startActivity(intent);
        if(getActivity() != null)
        getActivity().finish();
    }
}
