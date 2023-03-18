//
//  GithubEmptySearch.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/16/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct GithubEmptySearch: View {
  let hasTerm: Bool

  var body: some View {
    if hasTerm {
      Text("Empty results")
        .font(.title3)
        .multilineTextAlignment(.center)
        .padding(10)
    } else {
      Text("Search github repositories...")
        .font(.title3)
        .multilineTextAlignment(.center)
        .padding(10)
    }
  }
}
