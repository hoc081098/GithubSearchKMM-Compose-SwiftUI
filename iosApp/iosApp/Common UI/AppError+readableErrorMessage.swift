//
//  AppError+readableErrorMessage.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/15/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import sharedSwift

extension AppError {
  var readableMessage: String {
    switch AppErrorKs(self) {
    case .apiException(let e):
      switch AppErrorApiExceptionKs(e) {
      case .networkException(_):
        return "A network error has occurred. Please try again."
      case .serverException(_):
        return "A server error has occurred. Please try again."
      case .timeoutException(_):
        return "A network timeout error has occurred. Please try again."
      case .unknownException(_):
        return "An unknown error has occurred. Please try again."
      }
    case .localStorageException(let e):
      switch AppErrorLocalStorageExceptionKs(e) {
      case .fileException(_):
        return "A file system error has occurred. Please try again."
      case .databaseException(_):
        return "A database error has occurred. Please try again."
      case .unknownException(_):
        return "An unknown error has occurred. Please try again."
      }
    case .unknownException(_):
      return "An unknown error has occurred. Please try again."
    }
  }
}
