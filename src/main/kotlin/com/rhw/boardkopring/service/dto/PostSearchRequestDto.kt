package com.rhw.boardkopring.service.dto

data class PostSearchRequestDto(
    val id: Long? = null,
    val title: String? = null,
    val createdBy: String? = null,
    val tag: String? = null,
)
