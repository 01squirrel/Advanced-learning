package com.example.learnningproject.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class PlayList {
    @PrimaryKey
    public long playlistId;
    public long userCreatorId;
    public String playlistName;
}
