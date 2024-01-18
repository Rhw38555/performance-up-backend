package com.rhw.boardkopring.repository

import com.rhw.boardkopring.domain.Like
import org.springframework.data.jpa.repository.JpaRepository

interface LikeRepository: JpaRepository<Like, Long> {
    fun countByPostId(postId: Long): Long
}
