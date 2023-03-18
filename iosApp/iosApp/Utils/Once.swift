//
//  Once.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/17/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation


class Once<Input, Output> {
  private var block: ((Input) -> Output)?
  private var cache: Output? = nil

  init(_ block: @escaping (Input) -> Output) {
    self.block = block
  }

  func once(_ input: Input) -> Output {
    if self.cache == nil, let block = self.block {
      self.cache = block(input)
      self.block = nil
    }
    return self.cache!
  }
}

