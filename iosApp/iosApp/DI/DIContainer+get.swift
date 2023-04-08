//
//  DIContainer+get.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/15/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

extension DIContainer {
  func get<T>(
    for type: T.Type = T.self,
    qualifier: Koin_coreQualifier? = nil,
    parameters: (() -> Koin_coreParametersHolder)? = nil
  ) -> T {
    self.get(
      type: type,
      qualifier: qualifier,
      parameters: parameters
    ) as! T
  }
}
