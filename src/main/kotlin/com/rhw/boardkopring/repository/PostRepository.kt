package com.rhw.boardkopring.repository

import com.rhw.boardkopring.domain.Post
import com.rhw.boardkopring.domain.QLike.like
import com.rhw.boardkopring.domain.QPost.post
import com.rhw.boardkopring.service.dto.PostSearchRequestDto
import com.rhw.boardkopring.service.dto.PostSummaryResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface PostRepository: JpaRepository<Post, Long>, CustomPostRepository {
}
interface CustomPostRepository {
    fun findPageBy(pageRequest: Pageable, postSearchReqeustDto: PostSearchRequestDto): Page<Post>
}

class CustomPostRepositoryImpl : CustomPostRepository, QuerydslRepositorySupport(Post::class.java) {
    override fun findPageBy(pageRequest: Pageable, postSearchReqeustDto: PostSearchRequestDto): Page<Post> {
        val result = from(post)
            .where(
                postSearchReqeustDto.title?.let { post.title.contains(it) },
                postSearchReqeustDto.createdBy?.let { post.createdBy.eq(it) },
            )
            .orderBy(post.createdAt.desc())
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .fetchResults()


        return PageImpl(result.results, pageRequest, result.total)
    }
}
