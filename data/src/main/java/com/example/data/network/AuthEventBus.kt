package com.example.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

interface AuthEventBus {
    val logoutEvents: SharedFlow<Unit>
    fun notifyLogout()
}

@Singleton
class AuthEventBusImpl @Inject constructor() : AuthEventBus {
    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = _logoutEvents

    override fun notifyLogout() {
        runBlocking { _logoutEvents.emit(Unit) }
    }
}