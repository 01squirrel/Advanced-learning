package com.example.learnningproject.database.dao;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.learnningproject.database.data.UserAndLists;
import com.example.learnningproject.database.entity.UserEntity;

import java.util.List;

@Dao
public interface UserPlayListDao {
    @Transaction
    @Query("SELECT * FROM userentity")
    List<UserAndLists> getUsersWithPlaylists();
    @Query("select * from userentity where userId in (:ids) ")
    List<UserEntity> loadUsers(List<Integer> ids);
}
