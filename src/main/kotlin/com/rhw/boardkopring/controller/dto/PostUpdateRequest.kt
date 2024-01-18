package com.rhw.boardkopring.controller.dto

import com.rhw.boardkopring.service.dto.PostUpdateRequestDto

data class PostUpdateRequest(
    val title: String,
    val content: String,
    val updatedBy: String,
    val tags: List<String> = emptyList(),
)

fun PostUpdateRequest.toDto() = PostUpdateRequestDto(
    title = title,
    content= content,
    updatedBy = updatedBy,
    tags = tags,
)
