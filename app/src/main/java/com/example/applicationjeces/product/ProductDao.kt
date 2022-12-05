package com.example.applicationjeces.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/* 데이터베이스 접근시 사용 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM product_table ORDER BY product_name ASC")
    fun getAll(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)    /* 같은 데이터가 있으면 무시함(RPLACE는 덮어씌움) */
    fun insert(product: Product)    /* suspend: 코루틴사용해서  product 추가 */

    @Delete
    fun delete(product: Product)

    @Query("SELECT * FROM product_table WHERE product_name LIKE :searchQuery")
    fun search(searchQuery : String) : Flow<List<Product>>
}