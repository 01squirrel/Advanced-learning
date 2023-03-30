package com.example.learnningproject.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//多对多关系数据实体类，表示交叉引用表，
@Entity(primaryKeys = {"playlistId","songId"})
public class PlayListSongCrossRef {
    public long playlistId;
    public long songId;
}
