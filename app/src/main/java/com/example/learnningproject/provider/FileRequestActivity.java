package com.example.learnningproject.provider;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.TextView;

import com.example.learnningproject.R;
import com.google.android.material.button.MaterialButton;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

//请求某个分享的文件
public class FileRequestActivity extends Activity {
    private Intent requestFileIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_request);
        requestFileIntent = new Intent(Intent.ACTION_PICK);
        requestFileIntent.setType("image/jpg");
        MaterialButton button = findViewById(R.id.mb_getFile);
        button.setOnClickListener(view -> requestFile());
    }
    protected void requestFile(){
        startActivity(requestFileIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            Log.e("TAG", "onActivityResult: request fail" );
        } else {
            Uri resultUri = data.getData();
            ParcelFileDescriptor inputPFD;
            try{
                inputPFD = getContentResolver().openFileDescriptor(resultUri,"r");
            }catch(FileNotFoundException exception){
                exception.printStackTrace();
                Log.e("FileRequestActivity", "File not found.");
                return;
            }
            //检索文件信息
            FileDescriptor fileDescriptor = inputPFD.getFileDescriptor();
            String type = getContentResolver().getType(resultUri);
            TextView textView;
            String info;
            try (Cursor returnCursor = getContentResolver().query(resultUri, null, null, null, null)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                textView = findViewById(R.id.tv_info);
                info = String.format(getString(R.string.file_info), returnCursor.getString(nameIndex), returnCursor.getLong(sizeIndex), type);
            }
            textView.setText(info);
        }
    }
}