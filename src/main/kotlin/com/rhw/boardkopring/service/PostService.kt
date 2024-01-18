package com.rhw.boardkopring.service

import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.exception.PostNotDeletableException
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.repository.TagRepository
import com.rhw.boardkopring.service.dto.*
import com.rhw.boardkopring.util.RedisUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val likeService: LikeService,
    private val tagRepository: TagRepository,
    private val redisUtil: RedisUtil,
) {
    @Transactional
    fun createPost(requestDto: PostCreateRequestDto): Long {
        return postRepository.save(requestDto.toEntity()).id
    }

    @Transactional
    fun updatePost(id: Long, requestDto: PostUpdateRequestDto): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        post.update(requestDto)
        return id
    }

    @Transactional
    fun deletePost(id: Long, deletedBy: String): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        if (post.createdBy != deletedBy) throw PostNotDeletableException()
        postRepository.delete(post)
        return id
    }

    fun getPost(id: Long): PostDetailResponseDto {
        lateinit var likeCount: Any
        if (redisUtil.getData(redisUtil.getLikeCountKey(id)) != null) {
            likeCount = convertToLong(redisUtil.getData(redisUtil.getLikeCountKey(id))!!) as Long
        } else {
            likeCount = likeService.countLike(id)
        }
        return postRepository.findByIdOrNull(id)?.toDetailResponseDto(likeCount)
            ?: throw PostNotFoundException()
    }

    fun findPageBy(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Page<PostSummaryResponseDto> {
        postSearchRequestDto.tag?.let {
            return tagRepository.findPageBy(pageRequest, it).toSummaryResponseDto(likeService::countLike)
        }
        return postRepository.findPageBy(pageRequest, postSearchRequestDto)
            .toSummaryResponseDto(likeService::countLike, redisUtil::getData, redisUtil::getLikeCountKey)
    }

    private fun convertToLong(value: Any?): Long? {
        return when (value) {
            is Long -> value // 이미 Long 타입인 경우
            is Int -> value.toLong() // Int 타입인 경우 Long으로 변환
            is Short -> value.toLong() // Short 타입인 경우 Long으로 변환
            is Byte -> value.toLong() // Byte 타입인 경우 Long으로 변환
            is Double -> value.toLong() // Double 타입인 경우 Long으로 변환 (소수점 이하 버림)
            is Float -> value.toLong() // Float 타입인 경우 Long으로 변환 (소수점 이하 버림)
            is String -> value.toLongOrNull() // String 타입인 경우 파싱, 실패하면 null 반환
            else -> null // 그 외의 경우에는 변환 불가능
        }
    }
}
