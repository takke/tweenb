package jp.takke.tweenb.app.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.takke.tweenb.app.domain.BsFeedViewPost
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import jp.takke.tweenb.app.domain.createdAtAsDate
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostItem(
  post: BsFeedViewPost,
  modifier: Modifier,
  columns: List<ColumnInfo>,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
  ) {
    var rowHeight by remember { mutableStateOf(0.dp) }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .onSizeChanged {
          rowHeight = it.height.dp
        }
        .fillMaxWidth()
    ) {
      columns.forEachIndexed { index, columnInfo ->
        when (columnInfo.type) {
          ColumnType.Icon -> {
            // アイコン
            UserIcon(columnInfo, post)
          }

          ColumnType.Name -> {
            // 名前
            Column(
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp)
            ) {
              Text(
                text = post.post.author?.displayName ?: "",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "@${post.post.author?.handle ?: ""}",
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          ColumnType.Post -> {
            // 投稿内容
            Text(
              text = post.post.record?.asFeedPost?.text ?: "",
              style = MaterialTheme.typography.body1,
              modifier = Modifier
                .width(columnInfo.width)
                .padding(vertical = 4.dp, horizontal = 8.dp),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }

          ColumnType.DateTime -> {
            // 日時
            val formattedDate = post.post.createdAtAsDate?.let { createdAt ->
              val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
              dateFormat.format(createdAt)
            } ?: ""
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.caption,
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp)
            )
          }
        }

        if (index < columns.size - 1) {
          VerticalDivider(
            height = rowHeight,
            color = Color.LightGray,
          )
        }
      }
    }

    Divider(
      color = Color.LightGray
    )
  }
}

@Composable
private fun UserIcon(
  columnInfo: ColumnInfo,
  post: BsFeedViewPost
) {
  Box(
    modifier = Modifier
      .width(columnInfo.width)
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    val avatarUrl = post.post.author?.avatar
    if (avatarUrl != null) {
      AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
          .data(avatarUrl)
          .crossfade(true)
          .build(),
        contentDescription = "ユーザーアイコン",
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
      )
    } else {
      // アバター画像がない場合はプレースホルダーを表示
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .drawWithContent {
            drawCircle(
              color = Color.LightGray
            )
          }
      )
    }
  }
}