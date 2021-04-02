package io.camtact.android.model.dto;

import androidx.annotation.Keep;

@Keep
public class User {

    private String uid;
    private String userName;
    private String email;
    private boolean requestMatch;
    private boolean getKicked;
    private String matchedWho;
    private boolean sanctioned;
    private long lastAccess;

    public User(String uid, String userName, String email, boolean requestMatch, boolean getKicked, String matchedWho, boolean sanctioned, long lastAccess) {
        this.uid = uid;
        this.userName = userName;
        this.email = email;
        this.requestMatch = requestMatch;
        this.getKicked = getKicked;
        this.matchedWho = matchedWho;
        this.sanctioned = sanctioned;
        this.lastAccess = lastAccess;
    }

    public User() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRequestMatch() {
        return requestMatch;
    }

    public void setRequestMatch(boolean requestMatch) {
        this.requestMatch = requestMatch;
    }

    public String getMatchedWho() {
        return matchedWho;
    }

    public void setMatchedWho(String matchedWho) {
        this.matchedWho = matchedWho;
    }

    public boolean isSanctioned() {
        return sanctioned;
    }

    public void setSanctioned(boolean sanctioned) {
        this.sanctioned = sanctioned;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public boolean isGetKicked() {
        return getKicked;
    }

    public void setGetKicked(boolean getKicked) {
        this.getKicked = getKicked;
    }
}
