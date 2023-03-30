package com.example.learnningproject.database.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.learnningproject.database.entity.PlayList;
import com.example.learnningproject.database.entity.PlayListSongCrossRef;
import com.example.learnningproject.database.entity.Song;

import java.util.List;

//多对多关系数据类，如果您想查询播放列表和每个播放列表所含歌曲的列表，则应创建一个新的数据类，
// 其中包含单个 Playlist 对象，以及该播放列表所包含的所有 Song 对象的列表。
public class PlaylistAndSongs {
    @Embedded
    PlayList playList;
    @Relation(
            parentColumn = "playlistId",
            entityColumn = "songId",
            associateBy = @Junction(PlayListSongCrossRef.class)
    )
    List<Song> songs;
}

//您想查询歌曲和每首歌曲所在播放列表的列表，则应创建一个新的数据类，其中包含单个 Song 对象，以及包含该歌曲的所有 Playlist 对象的列表。
// public class SongWithPlaylists {
//    @Embedded public Song song;
//    @Relation(
//            parentColumn = "songId",
//            entityColumn = "playlistId",
//            associateBy = @Junction(PlaylistSongCrossRef.class)
//    )
//    public List<Playlist> playlists;
//}
