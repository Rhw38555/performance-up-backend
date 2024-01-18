package com.rhw.boardkopring.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.rhw.boardkopring.event.dto.LikeEvent
import com.rhw.boardkopring.repository.LikeRepository
import com.rhw.boardkopring.repository.PostRepository
import com.rhw.boardkopring.util.RedisUtil
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
    private val redisUtil: RedisUtil,
//    private val applicationEventPublisher: ApplicationEventPublisher,
    private val rabbitTemplate: RabbitTemplate,
) {
    @Value("\${rabbitmq.like.exchange}")
    private lateinit var likeExchange: String

    @Value("\${rabbitmq.like.routing-key}")
    private lateinit var likeRoutingKey: String

    fun createLike(postId: Long, createdBy: String) {
//    @Transactional
//    fun createLike(postId: Long, createdBy: String): Long {
//        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
//        redisUtil.increment(redisUtil.getLikeCountKey(postId))
//        return likeRepository.save(Like(post, createdBy)).id
//        applicationEventPublisher.publishEvent(LikeEvent(postId, createdBy))
        val objectMapper = ObjectMapper()
        val objectToJSON = objectMapper.writeValueAsString(LikeEvent(postId, createdBy))
        // direct exchange
        rabbitTemplate.convertAndSend(likeExchange, likeRoutingKey, objectToJSON)
    }

    fun countLike(postId: Long): Long {
        redisUtil.getCount(redisUtil.getLikeCountKey(postId))?.let { return it }
        with(likeRepository.countByPostId(postId)){
            redisUtil.setData(redisUtil.getLikeCountKey(postId), this)
            return this
        }
    }

}
