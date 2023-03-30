package com.example.learnningproject.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//定义一对一关系，创建实体类，其中一个实体类中需要一个变量用来表示对另一个实体的主键的引用
@Entity
public class Library {
    @PrimaryKey
    public long libraryId;
    public String name;
    public long userOwnerId;
}
