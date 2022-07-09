package com.hoc081988.github_search_kmm

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface AppModule {
  @Binds
  @Singleton
  fun appCoroutineDispatchers(impl: AndroidAppCoroutineDispatchers): AppCoroutineDispatchers
}
