package com.example.domain.model

/** Статус заявки на отгрузку. */
enum class LoadStatus(val code: Int) {
    IN_QUEUE(0),
    COMPLETED(1),
    DEFECT(-1),
    CANCELLED(-2),
    WEIGHED(3),
    UNKNOWN(-99);

    companion object {
        /** Возвращает статус по коду сервера или [UNKNOWN]. */
        fun fromCode(code: Int): LoadStatus = entries.find { it.code == code } ?: UNKNOWN
    }
}