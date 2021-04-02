package io.camtact.android.presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.auth.FirebaseAuth;

import io.camtact.android.model.SetModel;
import io.camtact.android.mvp_interface.SetMVP;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class SetPresenter implements SetMVP.Presenter {

    private SetMVP.View setView;
    private SetModel setModel;

    public SetPresenter(SetMVP.View view) {
        setView = view;
        setModel = new SetModel(this);
    }

    @Override
    public boolean checkOnlineStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void getUserName() {
        setModel.getUserName();
    }

    @Override
    public void editUserName(String userName) {
        setModel.editUserName(userName);
    }

    @Override
    public void outOfMembership() {
        setModel.outOfMembership();
    }

    @Override
    public void logout(boolean isLeaving) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        setView.goAuthActivity(isLeaving);
    }

    @Override
    public void sendInquiry(String inquiry) {
        setModel.sendInquiry(inquiry);
    }

    @Override
    public void sendError(String error) {
        setModel.reportError(error);
    }


    //call by Model
    @Override
    public void setUserName(String userName) {
        setView.setUserName(userName);
    }

    @Override
    public void hideProgressBar() {
        setView.hideProgressBar();
    }

    @Override
    public void showLogoutDialog() {
        setView.showLogoutDialog();
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean onTheTop) {
        setView.showSnackBar(message, milliTime, onTheTop);
    }

}
