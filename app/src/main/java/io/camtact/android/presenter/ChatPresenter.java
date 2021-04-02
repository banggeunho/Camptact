package io.camtact.android.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.camtact.android.model.ChatModel;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.mvp_interface.ChatMVP;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class ChatPresenter implements ChatMVP.Presenter {

    private static ChatMVP.View chatView;
    private ChatModel chatModel;

    private static final String TAG = "ChatPresenter";
    private static final int FILE_SIZE_LIMIT = 12000000;

    private String friendUid;

    public static StorageTask uploadTask;

    private Uri imageUri;
    private Bitmap resizedBitmap;
    private File toDeleteFile;

    public static Thread timeCheckThread;
    public static final int TIME_LIMIT = 900; //제한시간 (초)
    public static boolean isThreadActive;
    private static String leftTime;

    public ChatPresenter(ChatMVP.View view) {
        // View 연결
        chatView = view;
        // Model 연결
        chatModel = new ChatModel(this);
    }

    @Override
    public boolean checkOnlineStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void getIntent(Intent intent) {
        //getIntent from MatchFragment
        //Identify who chatting with me
        friendUid = intent.getStringExtra("uid");
    }

    @Override
    public void applyFriendNameAndReadMsg() {
        chatModel.applyFriendNameAndReadMsg(friendUid);
    }

    @Override
    public void checkChatRoom() {
        chatModel.checkChatRoom();
    }

    @Override
    public void checkMessage(String msg) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && msg.trim().length() > 0) {   //if ChatEditText is not empty
            chatModel.sendMessage(firebaseUser.getUid(), msg);
        }
    }

    @Override
    public void seenMessage() {
        chatModel.seenMessage();
    }

    @Override
    public void readMessage() {
        chatModel.readMessage();
    }

    @Override
    public void showLeftTime() {
        chatModel.showLeftTime();
    }

    @Override
    public void removeWholeListener() {
        chatModel.removeWholeListener();
    }

    @Override
    public void finishActivity(int reason) {
        chatView.finishActivity(reason);
    }

    @Override
    public void getImage(int requestCode, int resultCode, Intent data, int IMAGE_REQUEST, Activity activity) {
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            imageUri = data.getData();

            //String filePath = uriToFilePath(activity, imageUri);
            //Log.e("ChatPresenter", "filePath :" + filePath);

            int imageSize = getImageSize(imageUri, activity);

            if(imageSize < FILE_SIZE_LIMIT) {    // 이미지 사이즈가 제한 보다 작으면

                /*resizedBitmap = resizeUriToBitmap(activity, imageUri, 500);
                Uri resizedImgUri = bitmapToUri(activity, resizedBitmap);*/

                if (uploadTask != null && uploadTask.isInProgress()) {
                    Log.e("chatPresenter", "Upload in progress");
                    chatView.showSnackBar("이미지 전송이 이미 진행중입니다. 완료 후 시도해주세요.", 3000, true);
                } else {
                    chatView.showProgressBar();
                    chatModel.uploadImage(activity, imageUri, toDeleteFile);
                }
            } else {    //15mb 이상이면
                chatView.showSnackBar("12MB 이상의 고용량 이미지는 전송할 수 없습니다.", 3000, true);
            }
        }
    }
    private int getImageSize(Uri imageUri, Activity activity) {
        int dataSize = 0;

        String scheme = imageUri.getScheme();

        if (scheme != null && scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            try {
                InputStream fileInputStream = activity.getContentResolver().openInputStream(imageUri);
                if(fileInputStream != null)
                dataSize = fileInputStream.available();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("ChatPresenter","File size in bytes 1 :" + dataSize);
        }

        return dataSize;
    }

    /*private Bitmap resizeUriToBitmap(Context context, Uri imageUri, int resize){
        Bitmap resizedBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            *//*while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }*//*

            options.inSampleSize = 2;   //값이 커질수록 더 이미지용량이 작아짐
            resizedBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options); //3번

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resizedBitmap;
    }*/

    /*private Uri bitmapToUri(Context context, Bitmap bitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);

        //이 과정으로 인해 갤러리에 사진이 생겨났으니 지워주기 위해 filePath를 가져온다.
        String filePath = uriToFilePath(context, Uri.parse(path));
        toDeleteFile = new File(filePath);

        return Uri.parse(path);
    }

    private String uriToFilePath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null );
        if(cursor != null) {
            cursor.moveToNext();
            String filePath = cursor.getString(cursor.getColumnIndex("_data"));
            cursor.close();

            return filePath;
        } else {
            return null;
        }
    }*/

    @Override
    public void deleteChatRoom() {
        chatModel.deleteChatRoom(true);
        chatView.finishActivity(-1);
        removeWholeListener();
        timeCheckThread.interrupt();
    }





    //call by Model
    @Override
    public void setFriendNameText(String userName) {
        chatView.setFriendNameText(userName);
    }

    @Override
    public void removeAllMsg() {
        chatView.removeAllMsg();
    }

    @Override
    public void addMsg(ChatRoom.Chat chat) {
        chatView.addMsg(chat);
    }

    @Override
    public void hideProgressBar() {
        chatView.hideProgressBar();
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean onTheTop) {
        chatView.showSnackBar(message, milliTime, onTheTop);
    }

    @Override
    public String getFileExtension(Uri uri, Activity activity) {
        ContentResolver contentResolver = activity.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /*@Override
    public void deleteFileFromMediaStore(ContentResolver contentResolver, File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[] {canonicalPath});

        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }*/

    @Override
    public void runTimeChecker(final long madeTime) {

        final Handler handler = new Handler();

        if(!isThreadActive) {
            isThreadActive = true;
            timeCheckThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        try {
                            leftTime = calculateLeftTime(madeTime);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    chatView.setLeftTime(leftTime);
                                }
                            });


                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            isThreadActive = false;
                            break;
                        }
                    }
                }
            });
            timeCheckThread.setDaemon(true);
            timeCheckThread.start();
        }
    }

    private String calculateLeftTime(long madeTime) {

        try {
            //현재 Date 구하기
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREAN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            //현재 Date를 시간 형태로 parsing 후 time 가져오기
            currentDate = dateFormat.parse(dateFormat.format(currentDate));
            long currentDateTime = currentDate.getTime();

            //초 단위로 표현
            long differenceSec = (currentDateTime - madeTime) / 1000;

            differenceSec -= TIME_LIMIT;  //30분과의 차이를 계산하기 위해 30분(1800초) 빼기

            long shownMin = differenceSec / 60;
            long shownSec = differenceSec % 60;

            String shownMinStr = String.valueOf(shownMin);
            String shownSecStr = String.valueOf(shownSec);

            if(!shownMinStr.contains("-") && !shownSecStr.contains("-")) {    //시간이 다되어 0초 이상으로 넘어가게 된다면!
                chatModel.deleteChatRoom(false);
                chatModel.removeWholeListener();
                timeCheckThread.interrupt();
                chatView.finishActivity(0);
            }

            if (shownSecStr.length() == 2 || shownSecStr.equals("0")) {   // 마이너스가 붙어있는 상태이므로 길이 2가 보여지는 한 자리 수
                shownSecStr = "0" + shownSecStr;
            }

            String calLeftTime = shownMinStr + ":" + shownSecStr;

            return calLeftTime.replaceAll("-", ""); //-값은 안보여주기
        }
        catch (ParseException e) {
            Log.e(TAG, "ParseException :" + e.getMessage());
            return "error";
        }
    }

    @Override
    public void report(String reason) {
        chatModel.report(reason);
    }

}
