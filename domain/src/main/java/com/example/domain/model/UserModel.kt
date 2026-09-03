package com.example.domain.model

data class UserModel (
    val id: Int,
    // val login: String,
    val fio: String,
    val position: String,
    val positionId: Int,
    val isEmployee: Boolean,
    val isActive: Boolean
)