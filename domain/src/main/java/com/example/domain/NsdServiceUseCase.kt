package com.example.domain

import com.example.domain.repository.NsdServiceRepository

class ObserveServicesUseCase private constructor(
    private val repo: NsdServiceRepository,
) {
    // operator fun invoke() = repo.services
}

class StartDiscoveryUseCase private constructor(
    private val repo: NsdServiceRepository,
) {
    // operator fun invoke(type: String) = repo.start(type)
}

class StopDiscoveryUseCase private constructor(
    private val repo: NsdServiceRepository,
) {
    // operator fun invoke() = repo.stop()
}
