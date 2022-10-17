//
//  UIColor+init.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 7/17/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import shared

extension UIColor {
  convenience init?(argbColor: ArgbColor) {
    if let argb = argbColor.argb {
      self.init(
        red: CGFloat(argb.red),
        green: CGFloat(argb.green),
        blue: CGFloat(argb.blue),
        alpha: CGFloat(argb.alpha)
      )
    } else {
      return nil
    }
  }
}
