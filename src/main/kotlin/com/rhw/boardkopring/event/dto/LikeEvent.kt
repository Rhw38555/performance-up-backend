package com.rhw.boardkopring.event.dto

data class LikeEvent(
    val postId: Long,
    val createdBy: String,
)
