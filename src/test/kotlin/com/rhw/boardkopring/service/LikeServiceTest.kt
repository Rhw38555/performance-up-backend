package com.rhw.boardkopring.service

import com.rhw.boardkopring.domain.Post
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.repository.LikeRepository
import com.rhw.boardkopring.repository.PostRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.GenericContainer

@SpringBootTest
class LikeServiceTest(
    private val likeService: LikeService,
    private val likeRepository: LikeRepository,
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
//    given("좋아요 생성시") {
//        val savedPost = postRepository.save(Post("rhw", "title", "content"))
//        When("인풋이 정상적으로 들어오면"){
//            val likeId = likeService.createLike(1L, "rhw")
//            then("좋아요 정상 생성 확인"){
//                val like = likeRepository.findByIdOrNull(likeId)
//                like shouldNotBe null
//                like?.createdBy shouldBe "rhw"
//            }
//        }
//        When("게시글 존재하지 않으면"){
//            then("존재하지 않는 게시글 예외 발생"){
//                shouldThrow<PostNotFoundException> { likeService.createLike(9999L, "rhw") }
//            }
//        }
//    }
})
