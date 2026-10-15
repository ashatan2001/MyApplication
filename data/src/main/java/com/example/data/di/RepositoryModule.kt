package com.example.data.di

import com.example.data.repository.*
import com.example.domain.event.AuthEventBus
import com.example.domain.event.AuthEventBusImpl
import com.example.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Связывает интерфейсы доменного слоя с реализациями из data-слоя.
 *
 * Используется [Binds] вместо [Provides], т.к. не требуют реализации
 * дополнительной конфигурации при создании.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindDstPointRepository(impl: DstPointRepositoryImpl): DstPointRepository

    @Binds
    abstract fun bindArchiveRepository(impl: ArchiveRepositoryImpl): ArchiveRepository

    @Binds
    abstract fun bindPersonRepository(impl: PersonRepositoryImpl): PersonRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindAuthEventBus(impl: AuthEventBusImpl): AuthEventBus
}
