package com.hoc081988.github_search_kmm.data

import javax.inject.Inject

internal class DaggerAppErrorMapperImpl @Inject constructor(platformAppErrorMapper: PlatformAppErrorMapper) :
  AppErrorMapperImpl(platformAppErrorMapper)
