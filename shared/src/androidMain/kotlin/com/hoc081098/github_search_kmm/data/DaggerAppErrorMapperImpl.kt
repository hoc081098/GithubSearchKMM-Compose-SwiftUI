package com.hoc081098.github_search_kmm.data

import javax.inject.Inject

internal class DaggerAppErrorMapperImpl @Inject constructor(platformAppErrorMapper: PlatformAppErrorMapper) :
  AppErrorMapperImpl(platformAppErrorMapper)
