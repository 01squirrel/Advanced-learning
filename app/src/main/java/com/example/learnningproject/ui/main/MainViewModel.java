package com.example.learnningproject.ui.main;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.learnningproject.R;
import com.example.learnningproject.broadcast.ManifestBroadcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainViewModel extends ViewModel {
    private MutableLiveData<String> currentText;
    public MutableLiveData<String> getCurrentText() {
        if(currentText == null) {
            currentText = new MediatorLiveData<>();
        }
        return currentText;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void shareInfo(){
        //分享信息
        Context context = MainFragment.newInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        // (Optional) Here we're setting the title of the content
        intent.putExtra(Intent.EXTRA_TITLE, "Send message");
        intent.setType("text/plain");
        //pendingIntent 用于获取用户何时分享以及分享的目标
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
                intent, PendingIntent.FLAG_IMMUTABLE);
        intent = Intent.createChooser(intent,"share info",pendingIntent.getIntentSender());
        if (context != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Emits a sample share {@link Intent}.
     */
    public void share(){
        Context context = MainFragment.newInstance().getContext();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,"send uri file");
        shareIntent.putExtra(Intent.EXTRA_TITLE,"shareShortcuts");
        ClipData data = getClip();
        if(data != null){
            shareIntent.setClipData(data);
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if(context != null) context.startActivity(Intent.createChooser(shareIntent,null));
    }
    public Uri saveImage() throws FileNotFoundException {
        Context ctx = MainFragment.newInstance().getContext();
        if (ctx != null){
            Bitmap bm = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher);
            String image_dir = "images";
            File path = new File(Environment.getExternalStorageDirectory(), image_dir);
            if(path.mkdirs()){
                FileOutputStream outputStream = new FileOutputStream(path+File.separator+"image.png");
                bm.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            }
            File imagePath = new File(Environment.getExternalStorageDirectory(), image_dir);
            File newFile = new File(imagePath,"image.png");
            return FileProvider.getUriForFile(ctx,"com.example.learnningproject.fileprovider",newFile);
        }
        return null;
    }
    private ClipData getClip(){
        try {
            Context context = MainFragment.newInstance().getContext();
            Uri contentUri = saveImage();
            assert context != null;
            return ClipData.newUri(context.getContentResolver(),"clipData",contentUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}