//
//  flows.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 7/14/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

extension Kotlinx_coroutines_coreFlow {
  @discardableResult
  func subscribeNonNullFlow<T: AnyObject>(
    scope: Kotlinx_coroutines_coreCoroutineScope,
    onValue: @escaping (T) -> Void,
    onError: ((KotlinThrowable) -> Void)? = nil,
    onComplete: (() -> Void)? = nil
  ) -> Closeable {
    NonNullFlowWrapper<T>(flow: self)
      .subscribe(scope: scope) {
      onValue($0)

    } onError: { error in
      onError?(error)
    } onComplete: {
      onComplete?()
    }
  }
}

