package com.example.applicationjeces.product

data class KakaoResponse(
    val documents: List<Document>
) {
    data class Document(
        val address: Address
    ) {
        data class Address(
            val address_name: String
        )
    }
}

