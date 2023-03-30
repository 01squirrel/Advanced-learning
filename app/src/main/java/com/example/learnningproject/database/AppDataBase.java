package com.example.learnningproject.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.learnningproject.database.dao.UserLibraryDao;
import com.example.learnningproject.database.dao.UserPlayListDao;
import com.example.learnningproject.database.dao.WordDao;
import com.example.learnningproject.database.entity.Library;
import com.example.learnningproject.database.entity.PlayList;
import com.example.learnningproject.database.entity.PlayListSongCrossRef;
import com.example.learnningproject.database.entity.Song;
import com.example.learnningproject.database.entity.UserEntity;
import com.example.learnningproject.database.entity.Word;

/**
 * 定义保存数据库的AppDataBase类，定义数据库的配置
 * 该类必须带有@database注解，列出所有数据库实体
 * 该类必须是抽象类，用于扩展roomdatabase
 * 该类包含与数据库关联的dao类，并持有无参数的抽象方法，返回dao实例
 */

@Database(entities = {UserEntity.class, PlayList.class, Song.class, Library.class, PlayListSongCrossRef.class, Word.class}
        ,version = 1,exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {
    public abstract UserPlayListDao userPlayListDao();
    public abstract UserLibraryDao userLibraryDao();
    public abstract WordDao wordDao();
}
