package com.example.learnningproject.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StorageUtil {
    private final String TAG = StorageUtil.class.getSimpleName();

    //添加媒体项至现有集合
    public Uri insertMediaAudio(ContentResolver resolver,String filename){
        Uri audioCollection;
        ContentValues values = new ContentValues();
        // Find all audio files on the primary external storage device.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else{
            audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        values.put(MediaStore.Audio.Media.DISPLAY_NAME,filename);

        return resolver.insert(audioCollection,values);
    }
    //检查文档元数据---uri
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void dumpImageMetadata(Uri uri, ContentResolver resolver){
        try (Cursor cursor = resolver.query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String displayName = "";
                if(index > 0 || index == 0){
                    displayName = cursor.getString(index);
                }
                Log.i(TAG, "Display Name: " + displayName);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        }
    }

    //打开文档位图
    public Bitmap getBitmapFromUri(Uri uri,ContentResolver resolver) throws IOException {
        ParcelFileDescriptor descriptor = resolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        descriptor.close();
        return bitmap;
    }

    //打开文档--输入流
    public String readTextFromUri(ContentResolver resolver,Uri uri) {
        StringBuilder sb = new StringBuilder();
        try{
            InputStream inputStream = resolver.openInputStream(uri);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //修改文档--uri
    public void alterDocument(Uri uri,ContentResolver resolver,byte[] bytes){
        try{
            ParcelFileDescriptor fileDescriptor = resolver.openFileDescriptor(uri,"w");
            FileOutputStream outputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
            outputStream.write(bytes);
            outputStream.close();
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //确定是否为虚拟文件
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isVirtualFile(Uri uri, Context context){
        if(!DocumentsContract.isDocumentUri(context,uri)){
            return false;
        }
        Cursor cursor = context.getContentResolver().query(uri,new String[]{DocumentsContract.Document.COLUMN_FLAGS},null,null);
        int flags = 0;
        if(cursor.moveToFirst()){
            flags = cursor.getInt(0);
        }
        cursor.close();
        return (flags & DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0;
    }

    //获取application中指定key的meta-data值
    public String getApplicationMetadata(Context context,String key){
        ApplicationInfo info;
        String metaData = null;
        try{
            PackageManager manager = context.getPackageManager();
            info = manager.getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
            metaData = String.valueOf(info.metaData.get(key));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return metaData;
    }


}
