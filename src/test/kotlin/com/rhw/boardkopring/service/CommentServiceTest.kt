package com.rhw.boardkopring.service

import com.rhw.boardkopring.domain.Comment
import com.rhw.boardkopring.domain.Post
import com.rhw.boardkopring.exception.CommentNotDeletableException
import com.rhw.boardkopring.exception.CommentNotUpdatableException
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.repository.CommentRepository
import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.service.dto.CommentCreateRequestDto
import com.rhw.boardkopring.service.dto.CommentUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.GenericContainer

@SpringBootTest
class CommentServiceTest(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
): BehaviorSpec({
    val redisContainer = GenericContainer<Nothing>("redis:5.0.3-alpine")
    beforeSpec {
        redisContainer.portBindings.add("16979:6379")
        redisContainer.start()
        // 스펙이 실행될 때 마다 컨테이너 실행
        listener(redisContainer.perSpec())
    }
    afterSpec {
        redisContainer.stop()
    }
    given("댓글 생성시"){
        val post = postRepository.save(Post(
            title = "게시글 제목",
            content = "게시글 내용",
            createdBy = "게시글 생성자",
        ))
        When("인풋이 정상적으로 들어오면"){
            val commentId = commentService.createComment(1L, CommentCreateRequestDto(
                content = "댓글 내용",
                createdBy = "댓글 생성자",
            ))
            then("정상 생성됨을 확인한다."){
                commentId shouldBeGreaterThan  0L
                val comment = commentRepository.findByIdOrNull(commentId)
                comment shouldNotBe null
                comment?.content shouldBe "댓글 내용"
                comment?.createdBy shouldBe "댓글 생성자"
            }
        }
        When("게시글이 존재하지 않으면"){
            then("게시글 존재하지 않음 예외가 발생"){
                shouldThrow<PostNotFoundException> {
                    commentService.createComment(9999L, CommentCreateRequestDto(
                        content = "댓글 내용",
                        createdBy = "댓글 생성자",
                    ))
                }
            }
        }
    }
    given("댓글 수정시") {
        val savedPost = postRepository.save(Post(
            title = "게시글 제목",
            content = "게시글 내용",
            createdBy = "게시글 생성자",
        ))
        val savedComment = commentRepository.save(Comment("댓글 내용", savedPost, "댓글 생성자"))
        When("인풋이 정상적으로 들어오면"){
            val updatedId = commentService.updateComment(savedComment.id, CommentUpdateRequestDto(
                content = "수정된 댓글 내용",
                updatedBy = "댓글 생성자",
            ))
            then("정상 수행"){
                updatedId shouldBe savedComment.id
                val updated = commentRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.content shouldBe "수정된 댓글 내용"
                updated?.updatedBy shouldBe "댓글 생성자"
            }
        }
        When("작성자와 수정자가 다르면"){
            then("수정할 수 없는 게시물 예외 발생 "){
                shouldThrow<CommentNotUpdatableException> {
                    commentService.updateComment(savedComment.id, CommentUpdateRequestDto(
                        content = "수정된 댓글 내용",
                        updatedBy = "수정된 댓글 생성자",
                    ))
                }
            }
        }
    }
    given("댓글 삭제시"){
        val savedPost = postRepository.save(Post(
            title = "게시글 제목",
            content = "게시글 내용",
            createdBy = "게시글 생성자",
        ))
        val savedComment1 = commentRepository.save(Comment("댓글 내용", savedPost, "댓글 생성자"))
        val savedComment2 = commentRepository.save(Comment("댓글 내용2", savedPost, "댓글 생성자2"))
        When("인풋이 정상적으로 들어오면") {
            val commentId =  commentService.deleteComment(savedComment1.id, "댓글 생성자")
            then("정상 삭제됨 확인 "){
                commentId shouldBe savedComment1.id
                commentRepository.findByIdOrNull(commentId) shouldBe null

            }
        }
        When("작성자와 삭제자가 다르면"){
            then("삭제할 수 없는 댓글 예외 발생 "){
                shouldThrow<CommentNotDeletableException> { commentService.deleteComment(savedComment2.id, "삭제자") }
            }
        }
    }
})
