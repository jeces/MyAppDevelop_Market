package com.example.applicationjeces.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/* android.arch.persistence.room를 import하게 됨 */
@Entity(tableName = "product_table")
data class Product(
    /* 자동 생성 키 idx 값 */
    @PrimaryKey(autoGenerate = true)
    var pid: Long,

    /* 상품 이름 */
    @ColumnInfo(name = "product_name")
    var product_name: String,

    /* 상품 가격 */
    @ColumnInfo(name = "product_price")
    var product_price: String,

    /* 상품 설명 */
    @ColumnInfo(name = "product_description")
    var product_description: String,

    /* 이미지 개수 카운트 */
    @ColumnInfo(name = "product_count")
    var product_count: Int

) {

}
