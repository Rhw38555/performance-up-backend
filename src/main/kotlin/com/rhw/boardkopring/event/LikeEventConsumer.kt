package com.rhw.boardkopring.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.rhw.boardkopring.domain.Like
import com.rhw.boardkopring.event.dto.LikeEvent
import com.rhw.boardkopring.exception.PostNotFoundException
import com.rhw.boardkopring.repository.LikeRepository
import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.util.RedisUtil
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LikeEventConsumer(
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val redisUtil: RedisUtil,
    @Value("\${rabbitmq.like.queue}")
    private val likeQueue: String,
    private val objectMapper: ObjectMapper,
) {
//    @Async
//    @TransactionalEventListener(LikeEvent::class)
    @RabbitListener(queues = ["\${rabbitmq.like.queue}"])
    fun receiveMessage(eventMessage: String){
        val event = objectMapper.readValue(eventMessage, LikeEvent::class.java)
        val post = postRepository.findByIdOrNull(event.postId) ?: throw PostNotFoundException()
        redisUtil.increment(redisUtil.getLikeCountKey(event.postId))
        likeRepository.save(Like(post, event.createdBy))
    }
}
