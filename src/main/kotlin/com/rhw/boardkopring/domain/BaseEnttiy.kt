package com.rhw.boardkopring.domain

import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime


@MappedSuperclass
abstract class BaseEntity(
    createdBy: String
) {
    val createdBy: String = createdBy
    val createdAt: LocalDateTime = LocalDateTime.now()

    // 외부에서 변경 불가능
    var updatedBy: String? = null
        protected set
    var updatedAt: LocalDateTime? = null
        protected set

    fun updatedBy(updatedBy: String) {
        this.updatedBy = updatedBy
        this.updatedAt = LocalDateTime.now()
    }
}


