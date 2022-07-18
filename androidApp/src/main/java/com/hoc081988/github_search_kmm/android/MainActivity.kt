package com.hoc081988.github_search_kmm.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hoc081988.github_search_kmm.Greeting
import com.hoc081988.github_search_kmm.presentation.DaggerGithubSearchViewModel
import dagger.hilt.android.AndroidEntryPoint

fun greet(): String {
  return Greeting().greeting()
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      GithubSearchKmmApp()
    }
  }
}
