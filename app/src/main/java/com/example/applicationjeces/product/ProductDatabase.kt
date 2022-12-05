package com.example.applicationjeces.product

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/* 데이터테이블을 접근하고 있을 때 메인 엑서스 포인트, 이곳을 통해 디바이스에 있는 데이터를 접근과 수정, 삭제 함 */
@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class ProductDatabase: RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        /* Volatile : 다른 thread에서 접근 가능하게 만드는 것입니다.*/
        @Volatile
        private var INSTANCE: ProductDatabase? = null

        fun getInstance(context: Context): ProductDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            /* synchronized는 새로운 데이터베이스를 instance시킵니다. */
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductDatabase::class.java,
                    "product_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}