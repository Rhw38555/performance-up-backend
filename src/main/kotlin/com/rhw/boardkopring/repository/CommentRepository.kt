package com.rhw.boardkopring.repository

import com.rhw.boardkopring.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository: JpaRepository<Comment, Long> {
}
