package com.example.applicationjeces.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/* android.arch.persistence.room를 import하게 됨 */
@Entity(tableName = "product_table")
data class Product(
    /* 자동 생성 키 */
    @PrimaryKey(autoGenerate = true)
    var pid: Long,

    @ColumnInfo(name = "product_name")
    var product_name: String,

    @ColumnInfo(name = "product_price")
    var product_price: String
) {

}
