//
//  flows.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/14/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine

private let emptyOnComplete = { }
private let defaultOnError = { (error: Error) in
  Napier.e(error: error, "Unhandled error")
  fatalError("Unhandled error = \(error)")
}

extension StateFlow {
  func typedValue<T>(_ type: T.Type = T.self) -> T { value as! T }
}

extension Flow {
  // MARK: Flow<T>
  @discardableResult
  func subscribeNonNullFlow<T: AnyObject>(
    _ type: T.Type = T.self,
    scope: CoroutineScope,
    onValue: @escaping (T) -> Void,
    onError: ((Error) -> Void)? = nil,
    onComplete: (() -> Void)? = nil
  ) -> JoinableAndCloseable {
    NonNullFlowWrapper<T>(flow: self).subscribe(
      scope: scope,
      onValue: onValue,
      onError: { throwable in
        (onError ?? defaultOnError)(throwable.asNSError())
      },
      onComplete: onComplete ?? emptyOnComplete
    )
  }

  func asNonNullPublisher<T: AnyObject>(_ type: T.Type = T.self) -> AnyPublisher<T, Error> {
    NonNullFlowPublisher(flow: self).eraseToAnyPublisher()
  }

  // MARK: - Flow<T?>
  @discardableResult
  func subscribeNullableFlow<T: AnyObject>(
    _ type: T.Type = T.self,
    scope: CoroutineScope,
    onValue: @escaping (T?) -> Void,
    onError: ((Error) -> Void)? = nil,
    onComplete: (() -> Void)? = nil
  ) -> JoinableAndCloseable {
    NullableFlowWrapper<T>(flow: self).subscribe(
      scope: scope,
      onValue: onValue,
      onError: { throwable in
        (onError ?? defaultOnError)(throwable.asNSError())
      },
      onComplete: onComplete ?? emptyOnComplete
    )
  }

  func asNullablePublisher<T: AnyObject>(_ type: T.Type = T.self) -> AnyPublisher<T?, Error> {
    NullableFlowPublisher(flow: self).eraseToAnyPublisher()
  }
}

// MARK: - NonNullFlowPublisher
private struct NonNullFlowPublisher<T: AnyObject>: Publisher {
  typealias Output = T
  typealias Failure = Error

  private let flow: Flow

  init(flow: Flow) {
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
    flow: Flow,
    subscriber: S
  ) {
    self.subscriber = subscriber

    let dispatcher = DIContainer.shared
      .get(for: AppCoroutineDispatchers.self)
      .unconfined
    let scope = CoroutineScopeKt.CoroutineScope(context: dispatcher)

    self.closable = flow.subscribeNonNullFlow(
      scope: scope,
      onValue: {
        _ = subscriber.receive($0)
      },
      onError: {
        subscriber.receive(completion: .failure($0))
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

// MARK: - NullableFlowPublisher
private struct NullableFlowPublisher<T: AnyObject>: Publisher {
  typealias Output = T?
  typealias Failure = Error

  private let flow: Flow

  init(flow: Flow) {
    self.flow = flow
  }

  func receive<S>(subscriber: S) where S: Subscriber, Error == S.Failure, T? == S.Input {
    let subscription = NullableFlowSubscription(
      flow: flow,
      subscriber: subscriber
    )
    subscriber.receive(subscription: subscription)
  }
}

private class NullableFlowSubscription<T: AnyObject, S: Subscriber>: Subscription where S.Input == T?, S.Failure == Error {

  private var subscriber: S?
  private var closable: Closeable?

  init(
    flow: Flow,
    subscriber: S
  ) {
    self.subscriber = subscriber

    let dispatcher = DIContainer.shared
      .get(for: AppCoroutineDispatchers.self)
      .unconfined
    let scope = CoroutineScopeKt.CoroutineScope(context: dispatcher)

    self.closable = flow.subscribeNonNullFlow(
      scope: scope,
      onValue: {
        _ = subscriber.receive($0)
      },
      onError: {
        subscriber.receive(completion: .failure($0))
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
