package com.rhw.boardkopring.service

import com.rhw.boardkopring.exception.CommentNotDeletableException
import com.rhw.boardkopring.exception.CommentNotFoundException
import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.exception.PostNotDeletableException
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.repository.CommentRepository
import com.rhw.boardkopring.service.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun createComment(postId: Long, createRequestDto: CommentCreateRequestDto): Long {
        val post = postRepository.findByIdOrNull(postId) ?: throw  PostNotFoundException()
        return commentRepository.save(createRequestDto.toEntity(post)).id
    }

    @Transactional
    fun updateComment(id: Long, updateRequestDto: CommentUpdateRequestDto): Long {
        val comment = commentRepository.findByIdOrNull(id) ?: throw CommentNotFoundException()
        comment.update(updateRequestDto)
        return comment.id
    }

    @Transactional
    fun deleteComment(id: Long, deletedBy: String): Long {
        val comment = commentRepository.findByIdOrNull(id) ?: throw CommentNotFoundException()
        if(comment.createdBy != deletedBy){
            throw CommentNotDeletableException()
        }
        commentRepository.delete(comment)
        return id
    }

}
