package io.camtact.android.listener;

import io.camtact.android.model.PostModel;

public interface OnPostListener {
    void onDelete(PostModel postInfo);
    void onModify();
}

