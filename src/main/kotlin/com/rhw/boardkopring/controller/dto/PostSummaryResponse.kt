package com.rhw.boardkopring.controller.dto

import com.rhw.boardkopring.service.dto.PostSummaryResponseDto
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

data class PostSummaryResponse(
    val id: Long,
    val title: String,
    val createdBy: String,
    val createdAt: String,
    val tag: String? = null,
    val likeCount: Long = 0,
)

fun Slice<PostSummaryResponseDto>.toResponse() = SliceImpl(
    content.map { it.toResponse() },
    pageable,
    hasNext(),
)

fun PostSummaryResponseDto.toResponse() = PostSummaryResponse(
    id, title, createdBy, createdAt, firstTag, likeCount,
)
