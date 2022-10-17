package com.hoc081098.github_search_kmm

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.URLByDeletingPathExtension
import platform.Foundation.lastPathComponent
import platform.Foundation.pathExtension
import platform.Foundation.stringWithContentsOfFile

actual fun readTextResource(resourceName: String): String {
  val url = NSURL.fileURLWithPath(resourceName)

  val path = NSBundle.mainBundle.pathForResource(
    name = "resources/" + url.URLByDeletingPathExtension()?.lastPathComponent,
    ofType = url.pathExtension
  )!!

  return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String
}
