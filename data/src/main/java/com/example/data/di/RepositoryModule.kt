package com.example.data.di

import com.example.data.repository.ArchiveRepositoryImpl
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.DstPointsRepositoryImpl
import com.example.data.repository.PersonRepositoryImpl
import com.example.domain.event.AuthEventBus
import com.example.domain.event.AuthEventBusImpl
import com.example.domain.repository.ArchiveRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.DstPointsRepository
import com.example.domain.repository.PersonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Связывает интерфейсы доменного слоя с реализациями из data-слоя.
 *
 * Используется [Binds] вместо [Provides], т.к. реализации не требуют
 * дополнительной конфигурации при создании.
 */
@Module
@InstallIn(SingletonComponent::class) // или ViewModelComponent::class
abstract class RepositoryModule {
    @Binds
    abstract fun bindPersonRepository(impl: PersonRepositoryImpl): PersonRepository
    @Binds
    abstract fun bindDstPointsRepository(impl: DstPointsRepositoryImpl): DstPointsRepository
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds
    abstract fun bindArchiveRepository(impl: ArchiveRepositoryImpl): ArchiveRepository
    @Binds
    abstract fun bindAuthEventBus(impl: AuthEventBusImpl): AuthEventBus
}
