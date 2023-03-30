package com.example.learnningproject.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {
    @PrimaryKey
    public long songId;
    public String songName;
    public String artist;
}
