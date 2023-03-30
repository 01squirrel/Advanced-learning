package com.example.learnningproject.provider;

import androidx.core.content.FileProvider;

import com.example.learnningproject.R;

public class MyFileProvider extends FileProvider {
    public MyFileProvider() {
        super(R.xml.file_paths);
    }
}
