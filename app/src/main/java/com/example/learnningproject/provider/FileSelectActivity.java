package com.example.learnningproject.provider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnningproject.R;
import com.example.learnningproject.adapter.FileAdapter;

import com.google.android.material.button.MaterialButton;

import java.io.File;

//共享文件，接收客户端文件请求之文件选择,最好是在服务器应用中
public class FileSelectActivity extends Activity {

    //array of files in the images subdirectory
    File[] imageFiles;
    //Array of filenames corresponding to imageFiles
    String[] imageFileNames;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        Intent resultIntent = new Intent("com.example.learnningproject.provider.ACTION_RETURN_FILE");
        // The path to the root of this app's internal storage
        File privateRootDir = getFilesDir();
        //the path to the "images" subdirectory
        File imageDir = new File(privateRootDir, "images");
        imageFiles = imageDir.listFiles();
        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED,null);
        if(imageFiles != null){
            for(int i = 0;i < imageFiles.length;i++){
                imageFileNames[i] = imageFiles[i].getAbsolutePath();
            }
        }
        RecyclerView fileList = findViewById(R.id.lv_file);
        FileAdapter adapter = new FileAdapter(imageFileNames);
        fileList.setAdapter(adapter);
        //响应文件选择，获得要与其他应用共享的文件的内容 URI
        adapter.setOnItemClickListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                File requestFile = new File(imageFileNames[i]);
                try{
                    //允许客户端应用访问该文件,向客户端应用授予访问权限
                    Uri fileUri = FileProvider.getUriForFile(FileSelectActivity.this,"com.example.learnningproject.fileprovider",requestFile);
                    if(fileUri != null){
                        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        //如需与请求文件的应用共享文件，请将包含内容 URI 和权限的 Intent 传递给 setResult()。
                        resultIntent.setDataAndType(fileUri,getContentResolver().getType(fileUri));
                        FileSelectActivity.this.setResult(RESULT_OK,resultIntent);
                    }else{
                        resultIntent.setDataAndType(null,"");
                        FileSelectActivity.this.setResult(RESULT_CANCELED,resultIntent);
                    }

                }catch(IllegalArgumentException exception){
                    Log.e("File Selector",
                            "The selected file can't be shared: " + requestFile);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        MaterialButton button = findViewById(R.id.mb_exit);
        button.setOnClickListener(view -> finish());
    }
}