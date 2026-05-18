package cl.catchgo.app.di

import cl.catchgo.app.data.repository.ApplicationsRepositoryImpl
import cl.catchgo.app.data.repository.AuthRepositoryImpl
import cl.catchgo.app.data.repository.HabilidadesRepositoryImpl
import cl.catchgo.app.data.repository.JobsRepositoryImpl
import cl.catchgo.app.data.repository.MatchingRepositoryImpl
import cl.catchgo.app.data.repository.ProfileRepositoryImpl
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.AuthRepository
import cl.catchgo.app.domain.repository.HabilidadesRepository
import cl.catchgo.app.domain.repository.JobsRepository
import cl.catchgo.app.domain.repository.MatchingRepository
import cl.catchgo.app.domain.repository.ProfileRepository
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

    @Binds
    @Singleton
    abstract fun bindHabilidadesRepository(impl: HabilidadesRepositoryImpl): HabilidadesRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindMatchingRepository(impl: MatchingRepositoryImpl): MatchingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: cl.catchgo.app.data.repository.NotificationRepositoryImpl): cl.catchgo.app.domain.repository.NotificationRepository
}
