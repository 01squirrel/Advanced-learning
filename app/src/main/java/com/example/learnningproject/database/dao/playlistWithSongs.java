package com.example.learnningproject.database.dao;

import androidx.paging.PagingSource;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.learnningproject.database.data.PlaylistAndSongs;
import com.example.learnningproject.database.entity.PlayList;

import java.util.List;


public interface playlistWithSongs {
    //该方法会查询数据库并返回查询到的所有 PlaylistWithSongs 对象。
    @Transaction
    @Query("SELECT * FROM Playlist")
    List<PlaylistAndSongs> getPlaylistsWithSongs();
    @Query("select * from playlist where playlistId like :id")
    PagingSource<Integer, PlayList> loadListPaging(int id);
}

//查询歌曲
//    @Transaction
//    @Query("SELECT * FROM Song")
//    public List<SongWithPlaylists> getSongsWithPlaylists();