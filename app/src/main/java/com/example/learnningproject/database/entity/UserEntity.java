package com.example.learnningproject.database.entity;

//ROOM 数据库，使用实体定义数据

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;


//您希望表具有不同的名称，请设置 @Entity 注解的 tableName 属性
//您希望列具有不同的名称，请将 @ColumnInfo 注解添加到该字段并设置 name 属性
@Entity
public class UserEntity {
    @PrimaryKey
    public int userId;
    @ColumnInfo
    public String firstName;
    @ColumnInfo
    public String lastName;
    @ColumnInfo
    public int age;
   // @Embedded public Address address;

    public UserEntity(int userId, String firstName, String lastName, int age) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    //重写该方法是为了提高hash效率，并保持和equals一致
    @Override
    public int hashCode() {
        return Objects.hash(userId,age);
    }

    //重写是为了判断对象是否在逻辑上是同一对象
    @Override
    public boolean equals(@Nullable Object obj) {
        //地址相等
        if(this == obj) return true;
        //非空性
        if(obj == null || getClass() != obj.getClass()) return false;
        UserEntity user = (UserEntity) obj;
        boolean idCheck = false;
        boolean ageCheck = false;
        if(user.userId == this.userId) idCheck = true;
        if(user.age == this.age) ageCheck = true;
        return idCheck && ageCheck;
    }
}

//创建嵌套对象
//public class Address {
//    public String street;
//    public String state;
//    public String city;
//
//    @ColumnInfo(name = "post_code") public int postCode;
//}
