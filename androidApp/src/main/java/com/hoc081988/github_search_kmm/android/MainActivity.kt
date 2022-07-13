package com.hoc081988.github_search_kmm.android

import android.os.Bundle
import android.widget.TextView
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
  private val vm by viewModels<DaggerGithubSearchViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val tv: TextView = findViewById(R.id.text_view)
    tv.text = greet()

    vm.toString()
  }
}
