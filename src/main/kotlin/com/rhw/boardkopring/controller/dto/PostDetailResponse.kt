package com.rhw.boardkopring.controller.dto

import com.rhw.boardkopring.service.dto.PostDetailResponseDto

data class PostDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdBy: String,
    val createdAt: String,
    val comments: List<CommentResponse> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Long = 0,
)

fun PostDetailResponseDto.toResponse() = PostDetailResponse(
    id, title, content, createdBy, createdAt, comments.map {it.toResponse() }, tags, likeCount
)
