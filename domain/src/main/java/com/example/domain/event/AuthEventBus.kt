package com.example.domain.event

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEventBusImpl @Inject constructor() : AuthEventBus {
    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = _logoutEvents

    override suspend fun notifyLogout() {
        _logoutEvents.emit(Unit)
    }
}
interface AuthEventBus {
    val logoutEvents: SharedFlow<Unit>
    suspend fun notifyLogout()
}