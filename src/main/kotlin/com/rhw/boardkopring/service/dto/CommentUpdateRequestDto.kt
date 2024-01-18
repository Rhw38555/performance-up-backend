package com.rhw.boardkopring.service.dto

import com.rhw.boardkopring.domain.Comment
import com.rhw.boardkopring.domain.Post

data class CommentUpdateRequestDto(
    val content: String,
    val updatedBy: String,
)

