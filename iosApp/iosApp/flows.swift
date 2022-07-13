//
//  flows.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 7/14/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine

private let emptyOnComplete = { }
private let defaultOnError = { (error: KotlinThrowable) in
  debugPrint("subscribeNonNullFlow: unhandle error = \(error)")
}

extension Kotlinx_coroutines_coreFlow {
  @discardableResult
  func subscribeNonNullFlow<T: AnyObject>(
    _ type: T.Type = T.self,
    scope: Kotlinx_coroutines_coreCoroutineScope,
    onValue: @escaping (T) -> Void,
    onError: ((KotlinThrowable) -> Void)? = nil,
    onComplete: (() -> Void)? = nil
  ) -> Closeable {
    NonNullFlowWrapper<T>(flow: self).subscribe(
      scope: scope,
      onValue: onValue,
      onError: onError ?? defaultOnError,
      onComplete: onComplete ?? emptyOnComplete
    )
  }

  func asNonNullPublisher<T: AnyObject>(_ type: T.Type = T.self) -> AnyPublisher<T, Error> {
    NonNullFlowPublisher(flow: self).eraseToAnyPublisher()
  }
}


private struct NonNullFlowPublisher<T: AnyObject>: Publisher {
  typealias Output = T
  typealias Failure = Error

  private let flow: Kotlinx_coroutines_coreFlow

  init(flow: Kotlinx_coroutines_coreFlow) {
    self.flow = flow
  }

  func receive<S>(subscriber: S) where S: Subscriber, Error == S.Failure, T == S.Input {
    let subscription = NonNullFlowSubscription(
      flow: flow,
      subscriber: subscriber
    )
    subscriber.receive(subscription: subscription)
  }
}

private class NonNullFlowSubscription<T: AnyObject, S: Subscriber>: Subscription where S.Input == T, S.Failure == Error {

  private var subscriber: S?
  private var closable: Closeable?

  init(
    flow: Kotlinx_coroutines_coreFlow,
    subscriber: S
  ) {
    self.subscriber = subscriber

    let scope = DIContainer.shared
      .get(for: ScopeProvider.self)
      .scope()

    self.closable = flow.subscribeNonNullFlow(
      scope: scope,
      onValue: {
        _ = subscriber.receive($0)
      },
      onError: {
        subscriber.receive(completion: .failure($0.asNSError()))
      },
      onComplete: {
        subscriber.receive(completion: .finished)
      }
    )
  }

  func request(_ demand: Subscribers.Demand) { }

  func cancel() {
    self.subscriber = nil

    self.closable?.close()
    self.closable = nil
  }
}
