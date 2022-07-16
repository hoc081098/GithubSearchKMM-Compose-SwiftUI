//
//  RepoItemRow.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 7/17/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import Kingfisher
import UIKit

struct RepoItemRow: View {
  let item: RepoItem

  private let urlOnce = Once<RepoItemRow, URL> { this in
    URL(string: this.item.owner.avatar)!
  }
  private var url: URL { self.urlOnce.once(self) }

  
  private let languageColorOnce = Once<RepoItemRow, SwiftUI.Color?> { this in
    if
      let hex = this.item.languageColor?.hexStringWithoutPrefix,
      let uiColor = UIColor.init(hexString: hex) {
      return .init(uiColor)
    } else {
      return nil
    }
  }
  private var languageColor: SwiftUI.Color? { self.languageColorOnce.once(self) }

  var body: some View {
    HStack {
      KFAnimatedImage(url)
        .configure { view in view.framePreloadCount = 3 }
        .cacheOriginalImage()
        .onFailure { e in print("err: url=\(url), e=\(e)") }
        .placeholder { p in ProgressView(p) }
        .fade(duration: 1)
        .forceTransition()
        .aspectRatio(contentMode: .fill)
        .frame(width: 72, height: 72)
        .cornerRadius(20)
        .shadow(radius: 5)
        .frame(width: 92, height: 92)

      VStack(alignment: .leading) {
        Text(item.name)
          .font(.headline)
          .lineLimit(2)
          .truncationMode(.tail)

        Spacer().frame(height: 10)

        Text("item.description")
          .font(.subheadline)
          .lineLimit(2)
          .truncationMode(.tail)

        Spacer().frame(height: 10)
        
        HStack {
          if let languageColor = self.languageColor {
            Circle()
              .fill(languageColor)
              .frame(width: 16, height: 16)
            
            Spacer().frame(width: 8)
          }
          
          Text(item.language ?? "Unknown language")
            .font(.subheadline)
            .foregroundColor(languageColor)
          
          Spacer().frame(width: 24)
          
          Image(systemName: "star.fill")
            .foregroundColor(.yellow)
          Text("\(item.starCount)")
        }

        Spacer().frame(height: 10)
      }.padding([.bottom, .trailing, .top])
    }
  }
}
