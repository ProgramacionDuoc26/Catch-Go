package cl.catchgo.app.di

import cl.catchgo.app.data.repository.ApplicationsRepositoryImpl
import cl.catchgo.app.data.repository.AuthRepositoryImpl
import cl.catchgo.app.data.repository.JobsRepositoryImpl
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.AuthRepository
import cl.catchgo.app.domain.repository.JobsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindJobsRepository(impl: JobsRepositoryImpl): JobsRepository

    @Binds
    @Singleton
    abstract fun bindApplicationsRepository(impl: ApplicationsRepositoryImpl): ApplicationsRepository
}
