package com.rhw.boardkopring.repository

import com.querydsl.jpa.JPAExpressions.select
import com.rhw.boardkopring.domain.Post
import com.rhw.boardkopring.domain.QPost.post
import com.rhw.boardkopring.service.dto.PostSearchRequestDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport


interface PostRepository: JpaRepository<Post, Long>, CustomPostRepository  {
}
interface CustomPostRepository {
    fun findPageByDefault(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Slice<Post>
    fun findPageByNoOffset(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Slice<Post>
}



class CustomPostRepositoryImpl : CustomPostRepository, QuerydslRepositorySupport(Post::class.java) {
    override fun findPageByDefault(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Slice<Post> {

        val subQuery = select(post.id)
            from(post)
            .where(
                postSearchRequestDto.title?.let { post.title.contains(it) },
                postSearchRequestDto.createdBy?.let { post.createdBy.eq(it) },
            )
            .orderBy(post.createdAt.desc())
            .limit(pageRequest.pageSize.toLong())
            .fetch()

        val result = from(post)
            .where(post.id.`in`(subQuery))
            .orderBy(post.createdAt.desc())
            .limit(pageRequest.pageSize.toLong())
            .fetch()


        val hasNext = result.size > pageRequest.pageSize
        // count query가 필요없으므로 Page -> Slice로 변경
        return SliceImpl(result.take(pageRequest.pageSize), pageRequest, hasNext)
    }

    override fun findPageByNoOffset(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Slice<Post> {
        val result =
                from(post)
                    .where(
                        postSearchRequestDto.title?.let { post.title.contains(it) },
                        postSearchRequestDto.createdBy?.let { post.createdBy.eq(it) },
                        postSearchRequestDto.id?.let { post.id.lt(it) }
                    )
                    .orderBy(post.id.desc())
                    .limit(pageRequest.pageSize.toLong())
                    .fetch()

        val hasNext = result.size > pageRequest.pageSize
        return SliceImpl(result.take(pageRequest.pageSize), pageRequest, hasNext)
    }
}
