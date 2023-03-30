package com.example.learnningproject.database;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learnningproject.base.BaseApplication;
import com.example.learnningproject.database.entity.Word;

import java.util.List;

public class WordViewModel extends AndroidViewModel {
    private final WordRepository repository;
    private final LiveData<List<Word>> mAllWords;

    public WordViewModel() {
        super(new BaseApplication());
        repository = new WordRepository();
        mAllWords = repository.getmAllWords();
    }
    public LiveData<List<Word>> getAllWords() { return mAllWords; }
    public void insert(Word word) { repository.insert(word); }
}
