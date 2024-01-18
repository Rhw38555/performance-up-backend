package com.rhw.boardkopring.service.dto

import com.rhw.boardkopring.domain.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

data class PostSummaryResponseDto(
    val id: Long,
    val title: String,
    val createdBy: String,
    val createdAt: String,
    val firstTag: String? = null,
    val likeCount: Long = 0,
)

fun Page<Post>.toSummaryResponseDto(countLike: (Long) -> Long, getData: (String) -> Any?, getLikeCountKey: (Long) -> String) = PageImpl(
    content.map {
        // redis cache 조회
        val cacheCount = getData(getLikeCountKey(it.id).toString())
        if( cacheCount != null ){
            it.toSummaryResponseDtoByCache(convertToLong(cacheCount) as Long)
        }else{
            it.toSummaryResponseDtoByDB(countLike)
        }
    },
    pageable,
    totalElements,
)

fun Post.toSummaryResponseDtoByCache(likeCount: Long) = PostSummaryResponseDto(
    id = id,
    title = title,
    createdBy = createdBy,
    createdAt = createdAt.toString(),
    firstTag = tags.firstOrNull()?.name,
    likeCount = likeCount,
)

fun Post.toSummaryResponseDtoByDB(countLike: (Long) -> Long) = PostSummaryResponseDto(
    id = id,
    title = title,
    createdBy = createdBy,
    createdAt = createdAt.toString(),
    firstTag = tags.firstOrNull()?.name,
    likeCount = countLike(id),
)

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
