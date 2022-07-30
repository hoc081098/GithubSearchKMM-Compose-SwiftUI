package com.hoc081098.github_search_kmm

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
internal annotation class IoAppCoroutineScope

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
internal annotation class MainAppCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
internal interface AppModule {
  @Binds
  @Singleton
  fun appCoroutineDispatchers(impl: AndroidAppCoroutineDispatchers): AppCoroutineDispatchers

  @Binds
  @Singleton
  @IoAppCoroutineScope
  fun ioAppCoroutineScope(impl: DaggerIoAppCoroutineScope): AppCoroutineScope

  @Binds
  @Singleton
  @MainAppCoroutineScope
  fun mainAppCoroutineScope(impl: DaggerMainAppCoroutineScopeImpl): AppCoroutineScope
}
