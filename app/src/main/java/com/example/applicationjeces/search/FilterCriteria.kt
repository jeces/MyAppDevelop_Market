package com.example.applicationjeces.search

data class FilterCriteria(
    val minPrice: Int?,
    val maxPrice: Int?,
    val category: String?, // 예시로 String을 사용했으나, 실제 카테고리 타입에 맞게 수정해야 함
    val includeSaleCompleted: Boolean,
    val sameRegion: Boolean,
    // 추가로 필요한 필터 조건들...
)
