package com.rhw.boardkopring.service.dto

import com.rhw.boardkopring.domain.Comment
import com.rhw.boardkopring.domain.Post

data class CommentCreateRequestDto(
    val content: String,
    val createdBy: String,
)

fun CommentCreateRequestDto.toEntity(post: Post) = Comment(
    content = content,
    createdBy = createdBy,
    post = post,
)
