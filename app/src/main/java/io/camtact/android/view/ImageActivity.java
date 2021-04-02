package io.camtact.android.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import io.camtact.android.R;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);

        //PhotoView 이미지 라이브러리 (확대 및 축소)
        PhotoView photoView = findViewById(R.id.photo_view);

        String imageURL = getIntent().getStringExtra("imageURL");

        RequestOptions sharedOptions = new RequestOptions()
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.fail_image)
                .priority(Priority.HIGH)
                .fitCenter();

        Glide.with(getApplicationContext())
                .load(imageURL)
                .apply(sharedOptions)
                .into(photoView);
    }
}
