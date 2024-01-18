package com.rhw.boardkopring.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableRabbit
class RabbitmqConfig {
    @Value("\${spring.rabbitmq.host}")
    private val host: String? = null

    @Value("\${spring.rabbitmq.port}")
    private val port = 0

    @Value("\${spring.rabbitmq.username}")
    private val username: String? = null

    @Value("\${spring.rabbitmq.password}")
    private val password: String? = null

    @Value("\${rabbitmq.like.exchange}")
    private lateinit var likeExchange: String

    @Value("\${rabbitmq.like.queue}")
    private lateinit var likeQueue: String

    @Value("\${rabbitmq.like.routing-key}")
    private lateinit var likeRoutingKey: String

    /**
     * 1. Exchange 구성합니다.
     * "hello.exchange" 라는 이름으로 Direct Exchange 형태로 구성하였습니다.
     */
    @Bean
    fun directExchange(): DirectExchange {
        return DirectExchange(likeExchange)
    }

    /**
     * 2. 큐를 구성합니다.
     * "hello.queue"라는 이름으로 큐를 구성하였습니다.
     */
    @Bean
    fun queue(): Queue {
        return Queue(likeQueue, false)
    }


    /**
     * 3. 큐와 DirectExchange를 바인딩합니다.
     * "hello.key"라는 이름으로 바인딩을 구성하였습니다.
     */
    @Bean
    fun binding(directExchange: DirectExchange?, queue: Queue?): Binding {
        return BindingBuilder.bind(queue).to(directExchange).with(likeRoutingKey)
    }


    /**
     * 4. RabbitMQ와의 연결을 위한 ConnectionFactory을 구성합니다.
     * Application.properties의 RabbitMQ의 사용자 정보를 가져와서 RabbitMQ와의 연결에 필요한 ConnectionFactory를 구성합니다.
     */
    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost(host!!)
        connectionFactory.port = port
        connectionFactory.username = username!!
        connectionFactory.setPassword(password!!)
        return connectionFactory
    }

    /**
     * 5. 메시지를 전송하고 수신하기 위한 JSON 타입으로 메시지를 변경합니다.
     * Jackson2JsonMessageConverter를 사용하여 메시지 변환을 수행합니다. JSON 형식으로 메시지를 전송하고 수신할 수 있습니다
     */
    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }


    /**
     * 6. 구성한 ConnectionFactory, MessageConverter를 통해 템플릿을 구성합니다.
     */
    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter
        return rabbitTemplate
    }

}
