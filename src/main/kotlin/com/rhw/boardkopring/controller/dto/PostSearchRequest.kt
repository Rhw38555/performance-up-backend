package com.rhw.boardkopring.controller.dto

import com.rhw.boardkopring.service.dto.PostSearchRequestDto
import org.springframework.web.bind.annotation.RequestParam

data class PostSearchRequest(
    @RequestParam
    val id: Long?,
    @RequestParam
    val title: String?,
    @RequestParam
    val createdBy: String?,
    @RequestParam
    val tag: String?,
)

fun PostSearchRequest.toDto() = PostSearchRequestDto(
    id = id,
    title = title,
    createdBy = createdBy,
    tag = tag,
)
