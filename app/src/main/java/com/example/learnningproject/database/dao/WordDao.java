package com.example.learnningproject.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.learnningproject.database.entity.Word;

import java.util.List;

public interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertWord(Word word);
    @Query("delete from word_table")
    void deleteAll();
    @Query("select * from word_table order by word asc")
    LiveData<List<Word>> getAlphabetizedWords();
}
