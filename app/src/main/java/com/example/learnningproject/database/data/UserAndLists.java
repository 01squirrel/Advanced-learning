package com.example.learnningproject.database.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.learnningproject.database.entity.PlayList;
import com.example.learnningproject.database.entity.UserEntity;

import java.util.List;

//一对多关系数据类,@relation 注解添加到子实体上,同时parentColumn设置为父实体主键列的名称
// entityColumn设置为引用父实体主键的子实体列名称
public class UserAndLists {
    @Embedded
    public UserEntity user;
    @Relation(
            parentColumn = "userId",
            entityColumn = "userCreatorId"
    )
    public List<PlayList> playLists;

}
