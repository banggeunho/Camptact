package io.camtact.android.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import io.camtact.android.mvp_interface.AuthMVP;

import static android.content.Context.MODE_PRIVATE;
import static io.camtact.android.model.MatchModel.requestMatch;

public class AuthModel implements AuthMVP.Model {

    private static final String TAG = "AuthModel";
    private static final String KEY_PENDING_EMAIL = "key_pending_email";

    private AuthMVP.Presenter authPresenter;

    public AuthModel(AuthMVP.Presenter presenter) {
        this.authPresenter = presenter;
    }

    @Override
    public void saveSharedPreferences(String pendingEmail, Activity activity) {

        //set SharedPreferences
        SharedPreferences sharedPreferences = activity.getSharedPreferences("auth", MODE_PRIVATE);
        //edit Key and Value
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PENDING_EMAIL, pendingEmail); // key, value를 이용하여 저장하는 형태
        editor.apply();
    }

    @Override
    public void openSharedPreferences(Activity activity) {
        //open SharedPreferences by file name
        SharedPreferences sharedPreferences = activity.getSharedPreferences("auth", MODE_PRIVATE);
        //default value is ""
        String savedPendingEmail = sharedPreferences.getString(KEY_PENDING_EMAIL,"");
        Log.d(TAG, "savedPendingEmail : " + savedPendingEmail);
        authPresenter.openSavedPendingEmail(savedPendingEmail);
    }

    @Override
    public void signInUser(String userUid, String userName, String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", userUid);
        hashMap.put("userName", userName);
        hashMap.put("email", email);
        hashMap.put("requestMatch", false);
        hashMap.put("getKicked", false);
        hashMap.put("matchedWho", null);
        hashMap.put("sanctioned", false);
        hashMap.put("lastAccess", ServerValue.TIMESTAMP);

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    requestMatch = false;
                    authPresenter.startMainActivity();
                } else {
                    authPresenter.showSnackbar("올바르지 않거나 만료된 회원가입 링크입니다.\n인증메일을 재전송해주십시오",3500, true, true);
                }
            }
        });
    }
}
