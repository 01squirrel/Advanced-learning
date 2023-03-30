package com.example.learnningproject.database;

import android.app.Application;
import android.net.wifi.WpsInfo;

import androidx.lifecycle.LiveData;

import com.example.learnningproject.base.BaseApplication;
import com.example.learnningproject.database.dao.WordDao;
import com.example.learnningproject.database.entity.Word;

import java.util.List;

/**
 * A Repository manages queries and allows you to use multiple backends.
 * In the most common example, the Repository implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
 */
public class WordRepository {
    private final WordDao mWordDao;
    private final LiveData<List<Word>> mAllWords;

    WordRepository(){
        AppDataBase db = new BaseApplication().getDbInstance();
        mWordDao = db.wordDao();
        mAllWords = mWordDao.getAlphabetizedWords();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Word>> getmAllWords(){
        return mAllWords;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Word word){
        BaseApplication.databaseWriteExecutor.execute(()-> mWordDao.insertWord(word));
    }
}
