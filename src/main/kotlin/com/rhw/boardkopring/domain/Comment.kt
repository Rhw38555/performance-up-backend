package com.rhw.boardkopring.domain

import com.rhw.boardkopring.exception.CommentNotUpdatableException
import com.rhw.boardkopring.service.dto.PostUpdateRequestDto
import com.rhw.boardkopring.exception.PostNotUpdatableException
import com.rhw.boardkopring.service.dto.CommentUpdateRequestDto
import jakarta.persistence.*

@Entity
class Comment(
    content: String,
    post: Post,
    createdBy: String,
): BaseEntity(createdBy = createdBy) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    var content: String = content
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    var post: Post = post
        protected set

    fun update(updatedRequestDto: CommentUpdateRequestDto) {
        if(updatedRequestDto.updatedBy != this.createdBy) {
            throw CommentNotUpdatableException()
        }
        this.content = updatedRequestDto.content
        super.updatedBy(updatedRequestDto.updatedBy)
    }
}
