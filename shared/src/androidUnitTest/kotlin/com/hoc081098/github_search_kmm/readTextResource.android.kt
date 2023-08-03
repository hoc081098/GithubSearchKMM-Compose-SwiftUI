package com.hoc081098.github_search_kmm

import java.io.File

actual fun readTextResource(resourceName: String): String =
  File("./src/commonTest/resources/$resourceName").readText()
