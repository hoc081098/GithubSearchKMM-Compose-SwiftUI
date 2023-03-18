//
//  Napier.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/17/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

extension Napier {
  static func v(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .verbose,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static func d(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .debug,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static func i(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .info,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static func w(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .warning,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static func e(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .error,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static func a(
    tag: String? = nil,
    error: Error? = nil,
    _ items: Any...,
    separator: String = " ",
    file: String = #file,
    function: String = #function
  ) {
    log(
      logLevel: .assert,
      error: error,
      tag: tag,
      items,
      separator: separator,
      file: file,
      function: function)
  }

  static private func log(
    logLevel: LogLevel,
    error: Error?,
    tag: String?,
    _ items: [Any],
    separator: String,
    file: String,
    function: String
  ) {
    let message = items.map { "\($0)" }.joined(separator: separator)

    let throwable: KotlinThrowable?
    if let error = error {
      throwable = NSErrorKt.asThrowable(error)
    } else {
      throwable = nil
    }

    Self.shared.log(
      priority: logLevel,
      tag: tag ?? Self.defaultTag(file: file, function: function),
      throwable: throwable,
      message: message
    )
  }

  private static func defaultTag(file: String, function: String) -> String {
    let fileName = URL(fileURLWithPath: file).lastPathComponent
    let functionName = String(function.prefix(while: { $0 != "(" }))
    return "\(fileName):\(functionName)"
  }
}
