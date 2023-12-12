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

extension Flow {
  // MARK: - Flow<T>

  func asNonNullPublisher<T: AnyObject>(
    _ type: T.Type = T.self,
    dispatcher: CoroutineDispatcher = Dispatchers.shared.Unconfined
  ) -> AnyPublisher<T, Error> {
    NonNullFlowPublisher(flow: self, dispatcher: dispatcher)
      .eraseToAnyPublisher()
  }

  // MARK: - Flow<T?>

  func asNullablePublisher<T: AnyObject>(
    _ type: T.Type = T.self,
    dispatcher: CoroutineDispatcher = Dispatchers.shared.Unconfined
  ) -> AnyPublisher<T?, Error> {
    NullableFlowPublisher(flow: self, dispatcher: dispatcher)
      .eraseToAnyPublisher()
  }
}

private func supervisorScope(dispatcher: CoroutineDispatcher) -> CoroutineScope {
  CoroutineScopeKt.CoroutineScope(
    context: dispatcher.plus(context: SupervisorKt.SupervisorJob(parent: nil))
  )
}

// MARK: - NonNullFlowPublisher

private struct NonNullFlowPublisher<T: AnyObject>: Publisher {
  typealias Output = T
  typealias Failure = Error

  private let flow: Flow
  private let dispatcher: CoroutineDispatcher

  init(flow: Flow, dispatcher: CoroutineDispatcher) {
    self.flow = flow
    self.dispatcher = dispatcher
  }

  func receive<S>(subscriber: S) where S: Subscriber, Error == S.Failure, T == S.Input {
    let subscription = NonNullFlowSubscription(
      flow: flow,
      subscriber: subscriber,
      dispatcher: dispatcher
    )
    subscriber.receive(subscription: subscription)
  }
}

private class NonNullFlowSubscription<T: AnyObject, S: Subscriber>: Subscription where S.Input == T, S.Failure == Error {

  private var subscriber: S?
  private var closable: Closeable?

  init(
    flow: Flow,
    subscriber: S,
    dispatcher: CoroutineDispatcher
  ) {
    self.subscriber = subscriber

    let wrapper = NonNullFlowWrapperKt.wrap(flow) as! NonNullFlowWrapper<T>
    self.closable = wrapper.subscribe(
      scope: supervisorScope(dispatcher: dispatcher),
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

// MARK: - NullableFlowPublisher

private struct NullableFlowPublisher<T: AnyObject>: Publisher {
  typealias Output = T?
  typealias Failure = Error

  private let flow: Flow
  private let dispatcher: CoroutineDispatcher

  init(flow: Flow, dispatcher: CoroutineDispatcher) {
    self.flow = flow
    self.dispatcher = dispatcher
  }

  func receive<S>(subscriber: S) where S: Subscriber, Error == S.Failure, T? == S.Input {
    let subscription = NullableFlowSubscription(
      flow: flow,
      subscriber: subscriber,
      dispatcher: dispatcher
    )
    subscriber.receive(subscription: subscription)
  }
}

private class NullableFlowSubscription<T: AnyObject, S: Subscriber>: Subscription where S.Input == T?, S.Failure == Error {

  private var subscriber: S?
  private var closable: Closeable?

  init(
    flow: Flow,
    subscriber: S,
    dispatcher: CoroutineDispatcher
  ) {
    self.subscriber = subscriber

    let wrapper = NullableFlowWrapperKt.wrap(flow) as! NullableFlowWrapper<T>
    self.closable = wrapper.subscribe(
      scope: supervisorScope(dispatcher: dispatcher),
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
