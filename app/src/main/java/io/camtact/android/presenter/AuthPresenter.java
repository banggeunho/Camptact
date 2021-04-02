package io.camtact.android.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.camtact.android.model.AuthModel;
import io.camtact.android.mvp_interface.AuthMVP;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static io.camtact.android.view.AuthActivity.adminMode;

public class AuthPresenter implements AuthMVP.Presenter {

    private static final String TAG = "AuthPresenter";
    private static final String KEY_PENDING_EMAIL = "key_pending_email";

    private AuthMVP.View authView;
    private AuthModel authModel;
    private FirebaseAuth firebaseAuth;

    private String pendingEmail;
    private String mEmailLink;

    public AuthPresenter(AuthMVP.View view) {
        // View 연결
        authView = view;
        // Model 연결
        authModel = new AuthModel(this);
    }

    @Override
    public boolean checkOnlineStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void initFirebaseAuth() {
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void checkEmailAddress(final String email) {

        authView.disableSendBtn();

        if(email == null || email.equals("")) {
            authView.showSnackBar("이메일을 입력해주세요",2500, true, true);
            authView.hideProgressBar();
            authView.enableSendBtn();

        } else if(adminMode) {
            //check email already exist or not.
            firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Log.e(TAG, "Invalid email address.");
                                authView.showSnackBar("올바른 이메일 형식이 아닙니다", 3000, true, true);
                                authView.hideProgressBar();
                                authView.enableSendBtn();
                            } else {
                                if(task.getResult() != null && task.getResult().getSignInMethods() != null) {
                                    boolean isExist = !task.getResult().getSignInMethods().isEmpty();

                                    if (isExist) {  //show notice that already signined email
                                        Log.w("TAG", "Email already exist");
                                        authView.showAlertDialog(email);
                                    } else {
                                        sendSignInLink(email);
                                    }
                                }
                                authView.hideProgressBar();
                                authView.enableSendBtn();
                            }
                        }
                    });
        } else {

            if (!email.contains("@gachon.ac.kr")) {
                authView.showSnackBar("가천대학교 웹메일(@gachon.ac.kr)이 아닙니다", 3000, true, true);
                authView.hideProgressBar();
                authView.enableSendBtn();
            } else {
                //check email already exist or not.
                firebaseAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                                Exception e = task.getException();
                                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                    Log.e(TAG, "Invalid email address.");
                                    authView.showSnackBar("올바른 이메일 형식이 아닙니다", 3000, true, true);
                                    authView.hideProgressBar();
                                    authView.enableSendBtn();
                                } else {
                                    if(task.getResult() != null && task.getResult().getSignInMethods() != null) {
                                        boolean isExist = !task.getResult().getSignInMethods().isEmpty();

                                        if (isExist) {  //show notice that already signined email
                                            Log.w("TAG", "Email already exist");
                                            authView.showAlertDialog(email);
                                        } else {
                                            sendSignInLink(email);
                                        }
                                    }
                                    authView.hideProgressBar();
                                    authView.enableSendBtn();
                                }
                            }
                        });
            }
        }
        authView.hideKeyboard();
    }

    @Override
    public void sendSignInLink(final String email) {

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                .setUrl("https://camtact.page.link/in")
                // This must be true
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                        "io.camtact.android",
                        false, /* installIfNotAvailable */
                        null    /* minimumVersion */)
                .build();

        authView.showProgressBar();

        firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        authView.hideProgressBar();
                        authView.enableSendBtn();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            authView.showSnackBar("인증 이메일 전송 완료!", 3500,false, true);
                            pendingEmail = email;
                        } else {
                            Exception e = task.getException();
                            Log.w(TAG, "Could not send link", e);
                            authView.showSnackBar("이메일 전송 실패. 다시 시도해주세요", 3500, true, true);
                        }
                    }
                });

    }


    /**
     * Check to see if the Intent has an email link, and if so set up the UI accordingly.
     * This can be called from either onCreate or onNewIntent, depending on how the Activity
     * was launched.
     */
    @Override
    public void checkIntent(@Nullable Intent intent) {
        if (intentHasEmailLink(intent) && intent != null && intent.getData() != null) {
            mEmailLink = intent.getData().toString();

            if(pendingEmail!=null && !pendingEmail.equals("")) {
                signInWithEmailLink(pendingEmail, mEmailLink);
            } else {
                authView.showSnackBar("올바르지 않거나 만료된 회원가입 링크입니다.\n인증메일을 재전송해주십시오",3500, true, true);
            }
        }

        if(intent != null && intent.getExtras() != null) {
            boolean isSanctioned = intent.getExtras().getBoolean("isSanctioned", false);
            boolean leave = intent.getExtras().getBoolean("leave", false);
            if(isSanctioned) {
                authView.showBanDialog();
            } else if(leave) {
                authView.showSnackBar("회원탈퇴가 정상적으로 완료됬습니다", 3000, true, true);
            }
        }
    }

    /**
     * Determine if the given Intent contains an email sign-in link.
     */
    @Override
    public boolean intentHasEmailLink(@Nullable Intent intent) {
        /*if (intent != null && intent.getData() != null) {
            String intentData = intent.getData().toString();
            if (firebaseAuth.isSignInWithEmailLink(intentData)) {
                return true;
            }
        }
        return false;*/
        //아래 코드가 잘 작동되면 위 코드 지우기


        if (intent != null && intent.getData() != null) {
            String intentData = intent.getData().toString();
            return firebaseAuth.isSignInWithEmailLink(intentData);
        } else {
            return false;
        }
    }

    /**
     * Sign in using an email address and a link, the link is passed to the Activity
     * from the dynamic link contained in the email.    And then add User info on realtime database
     */
    @Override
    public void signInWithEmailLink(final String email, String link) {
        Log.e(TAG, "email :" + email);
        Log.e(TAG, "signInWithLink:" + link);

        authView.showProgressBar();

        firebaseAuth.signInWithEmailLink(email, link)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        authView.hideProgressBar();
                        pendingEmail = null;

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmailLink:success");
                            authView.showSnackBar("가입 완료!",3500, true, true);

                            //add User on database
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if(firebaseUser != null) {
                                String userUid = firebaseUser.getUid();
                                String userName = userUid.substring(0, 6);   //cut characters at 6st character

                                authModel.signInUser(userUid, userName, email);
                            }

                        } else {
                            Log.e(TAG, "signInWithEmailLink:failure", task.getException());
                            if(task.getException() != null && task.getException().getMessage().contains("disabled")) {
                                authView.showBanDialog();
                            }

                            if (task.getException() instanceof FirebaseAuthActionCodeException) {
                                Log.e(TAG, "signInWithEmailLink:failure " +  "FirebaseAuthActionCodeException");
                                authView.showSnackBar("올바르지 않거나 만료된 회원가입 링크입니다",3500, true, true);
                            }
                        }
                    }
                });

    }

    @Override
    public void autoLogin() {
        // Check if User is signed in (non-null) and update UI accordingly.
        if(firebaseAuth.getCurrentUser() != null){
            authView.startMainActivity();
        }
    }

    @Override
    public void outState(Bundle outState) {
        outState.putString(KEY_PENDING_EMAIL, pendingEmail);
    }

    // Restore the "pending" email address
    @Override
    public void savedInstanceState(Bundle savedInstanceState) {
        pendingEmail = savedInstanceState.getString(KEY_PENDING_EMAIL, null);
    }

    @Override
    public void saveSharedPreferences(Activity activity) {
        authModel.saveSharedPreferences(pendingEmail, activity);
    }

    @Override
    public void openSharedPreferences(Activity activity) {
        authModel.openSharedPreferences(activity);
    }



    //Call by Model

    @Override
    public void openSavedPendingEmail(String savedPendingEmail) {
        pendingEmail = savedPendingEmail;
    }

    @Override
    public void startMainActivity() {
        authView.startMainActivity();
    }

    @Override
    public void showSnackbar(String message, int milliTime, boolean simple, boolean onTheTop) {
        authView.showSnackBar(message, milliTime, simple, onTheTop);
    }
}

