package com.hoc081098.github_search_kmm.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hoc081098.github_search_kmm.android.R
import com.hoc081098.github_search_kmm.android.compose_utils.StableWrapper
import com.hoc081098.github_search_kmm.android.core_ui.AppTheme
import com.hoc081098.github_search_kmm.android.core_ui.fromArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import java.text.DecimalFormat
import kotlinx.datetime.Clock

@Composable
internal fun GithubRepoItemRow(
  item: RepoItem,
  decimalFormat: StableWrapper<DecimalFormat>,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  Card(
    modifier = modifier,
    elevation = CardDefaults.elevatedCardElevation(
      defaultElevation = 3.dp,
    ),
    shape = RoundedCornerShape(size = 20.dp),
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
        modifier = Modifier
          .size(92.dp)
          .clip(RoundedCornerShape(size = 20.dp))
          .background(Color.White),
        model = remember(context, item.owner.avatar) {
          ImageRequest.Builder(context)
            .data(item.owner.avatar)
            .crossfade(true)
            .build()
        },
        placeholder = painterResource(R.drawable.icons8_github_96),
        contentDescription = "Avatar",
        contentScale = ContentScale.FillBounds,
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          item.fullName,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          item.repoDescription ?: "",
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val languageColor = item.languageColor?.let(Color::fromArgbColor)

          languageColor?.let { color ->

            Canvas(
              modifier = Modifier.size(16.dp),
              onDraw = {
                drawCircle(
                  color = color,
                )
              },
            )

            Spacer(modifier = Modifier.width(8.dp))
          }

          Text(
            text = item.language ?: "Unknown language",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = languageColor ?: MaterialTheme.typography.bodyMedium.color,
            ),
          )

          Spacer(modifier = Modifier.width(24.dp))

          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star count",
            tint = Color(0xffEAC54E),
          )

          Text(
            text = decimalFormat.value.format(item.starCount),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun GithubRepoItemRowPreview() {
  AppTheme {
    GithubRepoItemRow(
      item = RepoItem(
        id = 2648,
        fullName = "Shawna Mercer",
        language = null,
        starCount = 6337,
        name = "Fletcher Mack",
        repoDescription = null,
        languageColor = null,
        htmlUrl = "https://search.yahoo.com/search?p=elit",
        owner = Owner(
          id = 5589,
          username = "Lucille Sears",
          avatar = "auctor",
        ),
        updatedAt = Clock.System.now(),
      ),
      decimalFormat = remember { StableWrapper(DecimalFormat("#,###")) },
    )
  }
}
