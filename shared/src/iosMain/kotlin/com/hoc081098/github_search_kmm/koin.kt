package com.hoc081098.github_search_kmm

import com.hoc081098.github_search_kmm.data.dataModule
import com.hoc081098.github_search_kmm.domain.domainModule
import com.hoc081098.github_search_kmm.presentation.presentationModule
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.ObjCObject
import kotlinx.cinterop.ObjCProtocol
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.KoinAppDeclaration

object DIContainer : KoinComponent {
  fun init(appDeclaration: KoinAppDeclaration = {}) {
    Napier.base(
      if (isDebug()) DebugAntilog()
      else object : Antilog() {
        override fun performLog(
          priority: LogLevel,
          tag: String?,
          throwable: Throwable?,
          message: String?
        ) {
          // TODO: Crashlytics
        }
      }
    )

    startKoin {
      appDeclaration()
      modules(
        dataModule,
        domainModule,
        appModule,
        presentationModule,
      )
      printLogger(
        if (isDebug()) {
          Level.DEBUG
        } else {
          Level.ERROR
        },
      )
    }
  }

  fun get(
    type: ObjCObject,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null
  ): Any? = getKoin().get(
    clazz = when (type) {
      is ObjCProtocol -> getOriginalKotlinClass(type)!!
      is ObjCClass -> getOriginalKotlinClass(type)!!
      else -> error("Cannot convert $type to KClass<*>")
    },
    qualifier = qualifier,
    parameters = parameters,
  )
}
