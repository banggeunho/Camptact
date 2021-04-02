package io.camtact.android.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import io.camtact.android.R;
import io.camtact.android.mvp_interface.AuthMVP;
import io.camtact.android.presenter.AuthPresenter;

public class AuthActivity extends AppCompatActivity implements AuthMVP.View {

    //프레젠터 선언
    AuthMVP.Presenter authPresenter;

    Button sendBtn;
    EditText emailEditText;
    TextView noticeText, makeMail, idNoticeText;
    ImageView logo;

    public static boolean adminMode = false;
    final String TAG = "AuthActivity";
    String email;
    int touched;

    InputMethodManager inputMethodManager;
    ProgressBar progressBar;

    private long backPressedTime= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        setupMVP();

        authPresenter.initFirebaseAuth();

        setupView();

        authPresenter.openSharedPreferences(this);
        // Restore the "pending" email address
        if (savedInstanceState != null) {
            authPresenter.savedInstanceState(savedInstanceState);
        }
        // Check if the Intent that started the Activity contains an email sign-in link.
        authPresenter.checkIntent(getIntent());

    }

    private void setupMVP() {
        authPresenter = new AuthPresenter(this);
    }

    private void setupView() {
        //init IMM for keyboard control
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        emailEditText = findViewById(R.id.email_editText);
        sendBtn = findViewById(R.id.send_mail_btn);

        noticeText = findViewById(R.id.notice_text);
        makeMail = findViewById(R.id.make_mail);
        idNoticeText = findViewById(R.id.id_notice_text);
        logo = findViewById(R.id.logo);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);  //make invisible. On create, there is no progressing working.

        //enter btn in keyboard action listener
        emailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch(actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        // 보내기 버튼
                        email = emailEditText.getText().toString();
                        authPresenter.checkEmailAddress(email);
                        break;
                    default:
                        // 기본 버튼
                        break;
                }
                return false;
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(authPresenter.checkOnlineStatus(AuthActivity.this)) {
                    showProgressBar();
                    email = emailEditText.getText().toString();
                    authPresenter.checkEmailAddress(email);
                } else {
                    showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true, true);
                }
            }
        });

        makeMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(authPresenter.checkOnlineStatus(AuthActivity.this)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gachon.ac.kr/site/join_nice.jsp")));
                } else {
                    showSnackBar("네트워크 연결 상태가 좋지 않습니다. 확인 후 다시 시도해주세요.", 3000, true, true);
                }
            }
        });

        idNoticeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIdNoticeDialog();
            }
        });

        touched = 0;
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touched ++;
                if(touched == 15) {
                    showSecretDialog();
                }
            }
        });
    }

    //뒤로가기 버튼을 두번 연속으로 눌러야 종료되게끔 하는 메소드
    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-backPressedTime>=2000){
            backPressedTime=System.currentTimeMillis();
            showSnackBar("뒤로가기 버튼을 한번 더 누르면 종료됩니다", 2000,true, false);
        }else if(System.currentTimeMillis()-backPressedTime<2000){
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        authPresenter.autoLogin();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        authPresenter.checkIntent(intent);
    }

    @Override
    public void disableSendBtn() {
        sendBtn.setEnabled(false);
    }

    @Override
    public void enableSendBtn() {
        sendBtn.setEnabled(true);
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean simple, boolean onTheTop) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, milliTime);
        if(onTheTop) {  //if the position of SnackBar is on the top
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
        if (!simple) {  //if not simple option, setAction go for checking Email.
            snackbar.setAction("이메일 확인하러 가기", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/mail")));
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideKeyboard() {
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void startMainActivity() {
        startActivity(new Intent(AuthActivity.this, SplashActivity.class));
        finish();
    }

    @Override
    public void showAlertDialog(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미 가입된 계정");
        builder.setMessage("이미 가입된 계정이어도 재로그인을 하기 위해선 본인확인을 위해 이메일 재인증이 필요합니다");
        builder.setPositiveButton("인증메일 전송",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        authPresenter.sendSignInLink(email);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void showBanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("사용 제한된 계정입니다. 제한 사유는 가입한 이메일을 확인해주세요");
        builder.setPositiveButton("이메일 확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/mail")));
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    private void showIdNoticeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("*회원 개인 정보에 대한 안내");
        builder.setMessage(" 본 어플에서 수집하는 개인 정보는 오직 가천대학교 웹메일뿐입니다.\n\n 가천대 웹메일에서 다른 메일로 '보내야만' 받은 사람 메일에서 학부와 이름이 공개됩니다. 본 어플의 인증 과정은 메일을 '받기'만 하는 것이라 신상정보가 노출되지 않습니다." +
                            "\n\n 관리자가 회원들의 웹메일을 알고 있다고 해서, 임의의 메일에서 가천대 웹메일로 보내는 것으로는 신상을 알 수가 없습니다. 학우분이 직접 본인의 가천대 웹메일을 이용하여 다른 메일로 보내야만 학부와 이름이 공개됩니다.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void showSecretDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        // EditText 삽입하기
        dialog.setMessage("                                          ");
        final EditText editText = new EditText(this);
        editText.setMaxLines(1);
        dialog.setView(editText);
        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                if(editText.getText().toString().equals("018130")) {
                    showSnackBar("일치!", 1500, true, false);
                    adminMode = true;
                }
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.e("SetFragment", "cancel");
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        dialog.show();
        editText.requestFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        authPresenter.outState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        authPresenter.saveSharedPreferences(this);
    }
}










