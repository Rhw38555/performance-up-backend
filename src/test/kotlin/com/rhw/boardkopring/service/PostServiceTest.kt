package com.rhw.boardkopring.service

import com.rhw.boardkopring.domain.Comment
import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.domain.Post
import com.rhw.boardkopring.domain.Tag
import com.rhw.boardkopring.service.dto.PostCreateRequestDto
import com.rhw.boardkopring.service.dto.PostSearchRequestDto
import com.rhw.boardkopring.service.dto.PostUpdateRequestDto
import com.rhw.boardkopring.exception.PostNotDeletableException
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.exception.PostNotUpdatableException
import com.rhw.boardkopring.repository.CommentRepository
import com.rhw.boardkopring.repository.TagRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.GenericContainer

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
    private val likeService: LikeService,
): BehaviorSpec({
    val redisContainer = GenericContainer<Nothing>("redis:5.0.3-alpine")

    beforeSpec {
        redisContainer.portBindings.add("16979:6379")
        redisContainer.start()
        // 스펙이 실행될 때 마다 컨테이너 실행
        listener(redisContainer.perSpec())

        postRepository.saveAll(
            listOf(
                Post("rhw", "title1", "content1", tags = listOf("tag1", "tag2")),
                Post("rhw", "title12", "content2", tags = listOf("tag1", "tag2")),
                Post("rhw", "title13", "content3", tags = listOf("tag1", "tag2")),
                Post("rhw", "title14", "content4", tags = listOf("tag1", "tag2")),
                Post("rhw", "title15", "content5", tags = listOf("tag1", "tag2")),
                Post("rhw2", "title6", "content6", tags = listOf("tag1", "tag5")),
                Post("rhw2", "title7", "content7", tags = listOf("tag1", "tag5")),
                Post("rhw2", "title8", "content8", tags = listOf("tag1", "tag5")),
                Post("rhw2", "title9", "content9", tags = listOf("tag1", "tag5")),
                Post("rhw2", "title10", "content10", tags = listOf("tag1", "tag5")),
            )
        )
    }
    afterSpec {
        redisContainer.stop()
    }
    given("게시글 생성 시 "){
        When("게시글 인풋이 정상적으로 들어오면"){
            val postId = postService.createPost(
                PostCreateRequestDto(
                title = "제목",
                content = "내용",
                createdBy = "rhw"
                )
            )
            then("게시글이 정상적으로 생성됨을 확인한다."){
                postId shouldBeGreaterThan  0L
                val post = postRepository.findByIdOrNull(postId);
                post shouldNotBe  null
                post?.title shouldBe "제목"
                post?.content shouldBe "내용"
                post?.createdBy shouldBe "rhw"
            }
        }
        When("태그가 추가되면"){
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "rhw",
                    tags = listOf("tag1", "tag2"),
                )
            )
            then("태그가 정상적으로 추가됨 확인"){
                val tags= tagRepository.findByPostId(postId)
                tags.size shouldBe 2
                tags[0].name shouldBe "tag1"
                tags[1].name shouldBe "tag2"
            }
        }
    }
    given("게시글 수정시") {
        val savedPost = postRepository.save(Post("rhw", "title", "content", listOf("tag1", "tag2")))
        When("정상 수정시"){
            val updatedId = postService.updatePost(savedPost.id, PostUpdateRequestDto(
                title = "update title",
                content = "update content",
                updatedBy = "rhw",
                )
            )
            then("게시글이 정상적으로 수정됨을 확인한다."){
                savedPost.id shouldBe updatedId
                val updated = postRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.title shouldBe "update title"
                updated?.content shouldBe "update content"
                updated?.updatedBy shouldBe "rhw"

            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을수없다 예외 발생 ") {
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(9999L, PostUpdateRequestDto(
                        title = "update title",
                        content = "update content",
                        updatedBy = "update rhw",
                    )
                    )
                }
            }
        }

        When("작성자가 동일하지 않으면") {
            then("수정할 수 없는 게시물입니다 예외 발생 ") {
                shouldThrow<PostNotUpdatableException> {
                    postService.updatePost(1L, PostUpdateRequestDto(
                        title = "update title",
                        content = "update content",
                        updatedBy = "update rhw",
                    )
                    )
                }
            }
        }
        When("태그가 수정되었을 때"){
            val updatedId = postService.updatePost(savedPost.id, PostUpdateRequestDto(
                title = "update title",
                content = "update content",
                updatedBy = "rhw",
                tags = listOf("tag1","tag2","tag3")
                )
            )
            then("정상적으로 수정됨 확인 "){
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag3"
            }
            then("태그 순서가 변경되었을때 정상 변경 확인") {
                postService.updatePost(savedPost.id, PostUpdateRequestDto(
                    title = "update title",
                    content = "update content",
                    updatedBy = "rhw",
                    tags = listOf("tag3","tag2","tag1")
                    )
                )
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag1"
            }
        }
    }

    given("게시글 삭제시") {
        val savedPost = postRepository.save(Post("rhw", "title", "content"))

        When("작성자가 동일하지 않으면") {
            then("삭제할수없다 에러 발생 "){
                shouldThrow<PostNotDeletableException> {
                    postService.deletePost(savedPost.id, "rhw22")
                }
            }
        }

        When("정상 삭제시"){
            val postId = postService.deletePost(savedPost.id, "rhw")
            then("게시글이 정상 삭제"){
                postId shouldBe savedPost.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }

    }

    given("게시글 상세조회시") {

        When("게시글이 존재하지 않으면") {
            then("게시글을 찾을 수 없다 예외 발생 "){
                shouldThrow<PostNotFoundException> {
                    postService.getPost(9999L)
                }
            }
        }

        val savedPost = postRepository.save(Post("rhw", "title", "content"))
        tagRepository.saveAll(
            listOf(
                Tag(name = "tag1", post = savedPost, createdBy = "rhw"),
                Tag(name = "tag2", post = savedPost, createdBy = "rhw"),
                Tag(name = "tag3", post = savedPost, createdBy = "rhw"),
            )
        )
        likeService.createLike(savedPost.id, "rhw")
        likeService.createLike(savedPost.id, "rhw1")
        likeService.createLike(savedPost.id, "rhw2")
        When("정상 조회시"){
            val post = postService.getPost(savedPost.id)
            then("게시글의 내용이 정상적으로 반환됨") {
                post.id shouldBe savedPost.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "rhw"
            }
            then("태그가 정상적으로 조회됨"){
                post.tags.size shouldBe 3
                post.tags[0] shouldBe "tag1"
                post.tags[1] shouldBe "tag2"
                post.tags[2] shouldBe "tag3"
            }
            then("좋아요 개수가 조회됨 확인"){
                post.likeCount shouldBe 3
            }
        }

        When("댓글 추가시"){
            commentRepository.save(Comment("댓글 내용1", savedPost, "댓글 작성자" ))
            commentRepository.save(Comment("댓글 내용2", savedPost, "댓글 작성자" ))
            commentRepository.save(Comment("댓글 내용3", savedPost, "댓글 작성자" ))
            val post = postService.getPost(savedPost.id)
            then("댓글이 함께 조회됨을 확인힌다."){
                post.comments.size shouldBe 3
                post.comments[0].content shouldBe "댓글 내용1"
                post.comments[1].content shouldBe "댓글 내용2"
                post.comments[2].content shouldBe "댓글 내용3"
                post.comments[0].createdBy shouldBe "댓글 작성자"
                post.comments[1].createdBy shouldBe "댓글 작성자"
                post.comments[2].createdBy shouldBe "댓글 작성자"
            }
        }
    }

    given("게시글 목록 조회시") {
        When("정상 조회시"){
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto())
            then("게시글 페이지가 반환된다.") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain  "title"
                postPage.content[0].createdBy shouldContain "rhw"
            }
        }

        When("타이틀로 검색"){
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(title="title1"))
            then("타이틀에 해당하는 게시글이 반환 "){
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain  "title1"
                postPage.content[0].createdBy shouldContain "rhw"
            }
        }

        When("작성자로 검색"){
            val postPage = postService.findPageBy(PageRequest.of(0, 3), PostSearchRequestDto(createdBy= "rhw"))
            then("작성자에 해당하는 게시글이 반환 "){
                postPage.number shouldBe 0
                postPage.size shouldBe 3
                postPage.content.size shouldBe 3
                postPage.content[0].title shouldContain  "title"
                postPage.content[0].createdBy shouldBe "rhw"
            }
            then("첫번째 태그가 함께 조회됨을 확인") {
                postPage.content.forEach {
                    it.firstTag shouldNotBe null
                }
            }
        }
        When("태그로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag= "tag5"))
            then("태그에 해당하는 게시글 찾기") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldBe "title10"
                postPage.content[1].title shouldBe "title9"
                postPage.content[2].title shouldBe "title8"
                postPage.content[3].title shouldBe "title7"
                postPage.content[4].title shouldBe "title6"
            }
        }
        When("좋아요 추가되었을떄"){
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag= "tag5"))
            postPage.content.forEach {
                likeService.createLike(it.id, "rhw1")
                likeService.createLike(it.id, "rhw2")
            }
            val likedPostPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag= "tag5"))
            then("좋아요 개수가 정상 조회됨 확인"){
                likedPostPage.content.forEach {
                    it.likeCount shouldBe 2
                }
            }
        }
    }
})
