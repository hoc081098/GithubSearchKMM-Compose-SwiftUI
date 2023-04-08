//
//  ErrorMessageAndButton.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/16/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct ErrorMessageAndButton: View {
  let error: AppError
  let onRetry: () -> Void
  var font: Font? = nil

  var body: some View {
    VStack(alignment: .center) {
      Text(error.readableMessage)
        .font(font ?? .title3)
        .multilineTextAlignment(.center)
        .padding(10)

      Button("Retry", action: onRetry)
        .buttonStyle(PlainButtonStyle.plain)

      Spacer().frame(height: 10)
    }.frame(maxWidth: .infinity)
  }
}
