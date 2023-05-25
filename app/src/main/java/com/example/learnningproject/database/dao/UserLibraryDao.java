package com.example.learnningproject.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.learnningproject.database.data.UserAndLibrary;
import com.example.learnningproject.database.entity.Library;
import com.example.learnningproject.database.entity.UserEntity;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

//定义对象之间的关系，中间数据类或者多重映射返回值
@Dao
public interface UserLibraryDao {
    @Query("SELECT userentity.firstName as userName,library.name as libraryName " +
            "from userentity,library " +
            "WHERE userentity.userId = library.userOwnerId")
     LiveData<List<UserLibrary>> loadUserLibrary();
    //根据所需的映射结构为方法定义多重映射返回值类型，并直接在 SQL 查询中定义实体之间的关系。
    @Query("select * from userentity join Library on userentity.userId = Library.userOwnerId")
     Map<UserEntity,List<Library>> loadUserAndLibrary();

    @Transaction
    @Query("select * from userentity")
    List<UserAndLibrary> getUserLibraries();
    @Insert
    void addUser(UserEntity entity);
    //异步单次查询
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUsers(List<UserEntity> entities);
    @Query("select * from userentity where userId = :id")
    Single<UserEntity> getUserById(int id);
    //编写可观察查询，指在查询引用的任何表发生更改时发出新值的读取操作。
    @Query("select * from userentity where userId = :id")
    Flowable<UserEntity> loadUserById(int id);
    @Delete
    void removeUser(UserEntity entity);

}
//中间数据类,表示已创建库的用户
class UserLibrary{
    public String userName;
    public String libraryName;
}
