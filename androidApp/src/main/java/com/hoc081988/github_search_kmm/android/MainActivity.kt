package com.hoc081988.github_search_kmm.android

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hoc081988.github_search_kmm.Greeting
import com.hoc081988.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import dagger.hilt.android.AndroidEntryPoint
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.launch

fun greet(): String {
  return Greeting().greeting()
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  @Inject
  lateinit var searchRepoItemsUseCase: SearchRepoItemsUseCase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val tv: TextView = findViewById(R.id.text_view)
    tv.text = greet()

    lifecycleScope.launch {
      searchRepoItemsUseCase(
        term = "kmm",
        page = 1
      ).let {
        Napier.d("searchRepoItemsUseCase: $it")
      }
    }
  }
}
