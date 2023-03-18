//
//  LazyView.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/18/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI

struct LazyView<Content: View>: View {
  let build: () -> Content

  init(_ build: @autoclosure @escaping () -> Content) {
    self.build = build
  }

  var body: Content {
    build()
  }
}
