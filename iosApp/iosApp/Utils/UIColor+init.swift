//
//  UIColor+init.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/17/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import shared

extension UIColor {
  convenience init(argbColor: ArgbColor) {
    let argb = argbColor.argb
    self.init(
      red: CGFloat(argb.red),
      green: CGFloat(argb.green),
      blue: CGFloat(argb.blue),
      alpha: CGFloat(argb.alpha)
    )
  }
}
