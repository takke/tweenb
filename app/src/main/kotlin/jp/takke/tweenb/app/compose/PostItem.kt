package jp.takke.tweenb.app.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.takke.tweenb.app.domain.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
  post: BsFeedViewPost,
  modifier: Modifier,
  columns: List<ColumnInfo>,
  openBrowser: (String) -> Unit
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

        if (index == 0) {
          VerticalDivider(
            height = rowHeight,
            color = Color.LightGray,
          )
        }

        when (columnInfo.type) {
          ColumnType.Icon -> {
            // アイコン
            UserIcon(
              columnInfo = columnInfo,
              post = post,
              onClick = {
                // ユーザーをブラウザで開く
                post.post.author?.url?.let { url ->
                  openBrowser(url)
                }
              }
            )
          }

          ColumnType.Name -> {
            // 名前
            Column(
              modifier = Modifier
                .width(columnInfo.width.value)
                .padding(8.dp)
            ) {
              Text(
                text = post.post.author?.displayName ?: "",
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .clickable {
                    // ユーザーをブラウザで開く
                    post.post.author?.url?.let { url ->
                      openBrowser(url)
                    }
                  }
              )
            }
          }

          ColumnType.ScreenName -> {
            // ScreenName(ユーザー名)
            Column(
              modifier = Modifier
                .width(columnInfo.width.value)
                .padding(8.dp)
            ) {
              Text(
                text = "@${post.post.author?.handle ?: ""}",
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .clickable {
                    // ユーザーをブラウザで開く
                    post.post.author?.url?.let { url ->
                      openBrowser(url)
                    }
                  }
              )
              val repostedBy = post.reason?.asReasonRepost?.by
              if (repostedBy != null) {
                Text(
                  text = "RP: @${repostedBy.handle}",
                  style = MaterialTheme.typography.caption,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(top = 4.dp)
                    .clickable {
                      // ユーザーをブラウザで開く
                      val url = repostedBy.url
                      openBrowser(url)
                    }
                )
              }
            }
          }

          ColumnType.Post -> {
            PostColumnContent(
              post = post,
              columnInfo = columnInfo
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
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .width(columnInfo.width.value)
                .padding(8.dp)
                .clickable {
                  // ポストのURIを取得して、ブラウザで開く
                  val url = post.post.url
                  if (url != null) {
                    openBrowser(url)
                  }
                }
            )
          }
        }

        VerticalDivider(
          height = rowHeight,
          color = Color.LightGray,
        )
      }
    }

    Divider(
      color = Color.LightGray
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostColumnContent(
  post: BsFeedViewPost,
  columnInfo: ColumnInfo
) {
  // 投稿内容
  val postBody = post.post.record?.asFeedPost?.text ?: ""
  val repostedBy = post.reason?.asReasonRepost?.by
  val displayText = if (repostedBy != null) {
    // リポスト
    "RP: $postBody"
  } else {
    postBody
  }

  // ツールチップエリアでラップ
  TooltipArea(
    tooltip = {
      // ツールチップの内容
      Surface(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = 4.dp
      ) {
        Text(
          text = displayText,
          style = MaterialTheme.typography.body2,
          modifier = Modifier
            .padding(10.dp)
            .widthIn(max = 600.dp) // 最大幅を設定
        )
      }
    },
    delayMillis = 300, // 表示までの遅延
    tooltipPlacement = TooltipPlacement.CursorPoint(
      alignment = Alignment.BottomStart
    )
  ) {
    // 通常表示の内容
    Text(
      text = displayText,
      style = MaterialTheme.typography.body2,
      modifier = Modifier
        .width(columnInfo.width.value)
        .padding(vertical = 4.dp, horizontal = 8.dp),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun UserIcon(
  columnInfo: ColumnInfo,
  post: BsFeedViewPost,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .width(columnInfo.width.value)
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    val avatarUrl = post.post.author?.avatar
    if (avatarUrl != null) {
      AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
          .data(avatarUrl)
          .crossfade(true)
          .size(160)
          .build(),
        contentDescription = "ユーザーアイコン",
        modifier = Modifier
          .clip(CircleShape)
          .size(40.dp)
          .clickable {
            onClick()
          },
        filterQuality = FilterQuality.High,
        contentScale = ContentScale.Crop
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