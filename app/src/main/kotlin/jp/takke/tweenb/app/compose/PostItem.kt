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
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import jp.takke.tweenb.app.util.BsFeedViewPost
import jp.takke.tweenb.app.util.createdAtAsDate
import jp.takke.tweenb.app.util.url
import work.socialhub.kbsky.model.app.bsky.embed.EmbedImagesViewImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
  post: BsFeedViewPost,
  modifier: Modifier,
  columns: List<ColumnInfo>,
  openBrowser: (String) -> Unit,
  visibleLines: Int
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
              columnInfo = columnInfo,
              visibleLines = visibleLines
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostColumnContent(
  post: BsFeedViewPost,
  columnInfo: ColumnInfo,
  visibleLines: Int
) {
  // 投稿内容（元のテキスト）
  val rawPostBody = post.post.record?.asFeedPost?.text ?: ""

  // 空行を削除した投稿内容を生成（タイムライン表示用）
  val postBody = rawPostBody.replace(Regex("\\n\\s*\\n"), "\n").trim()

  val repostedBy = post.reason?.asReasonRepost?.by

  // 返信情報を取得
  val isReply = post.post.record?.asFeedPost?.reply != null

  // プレフィックス部分を構築
  val prefix = buildString {
    // 返信の場合は「Re: 」を追加
    if (isReply) {
      append("Re: ")
    }

    // リポストの場合は「RP: 」を追加
    if (repostedBy != null) {
      append("RP: ")
    }
  }

  // タイムライン表示用テキスト（空行削除）
  val displayText = prefix + postBody

  // ポップアップ表示用テキスト（元のテキスト）
  val tooltipText = prefix + rawPostBody

  // 画像URLを取得
  val images = post.post.embed?.asImages?.images
  val hasImages = !images.isNullOrEmpty()

  TooltipArea(
    tooltip = {
      // ツールチップの内容（元のテキスト表示）
      PostTooltipContent(tooltipText, images, hasImages)
    },
    delayMillis = 500, // 表示までの遅延
    tooltipPlacement = TooltipPlacement.CursorPoint(
      alignment = Alignment.BottomStart
    )
  ) {
    // 通常表示の内容（空行削除済み）
    PostRowContent(displayText, columnInfo, visibleLines, images, hasImages)
  }
}

/**
 * ポストのツールチップ
 */
@Composable
private fun PostTooltipContent(
  tooltipText: String,
  images: List<EmbedImagesViewImage>?,
  hasImages: Boolean
) {
  Surface(
    modifier = Modifier.padding(8.dp),
    shape = RoundedCornerShape(4.dp),
    elevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .padding(10.dp)
        .widthIn(max = 600.dp) // 最大幅を設定
    ) {
      // テキスト表示
      Text(
        text = tooltipText,
        style = MaterialTheme.typography.body2
      )

      // 画像がある場合は表示
      if (hasImages) {
        Column(
          modifier = Modifier.padding(top = 8.dp)
        ) {
          images?.forEach { image ->
            val fullImage = image.fullsize
            if (fullImage != null) {
              AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                  .data(fullImage)
                  .crossfade(true)
                  .size(300)
                  .build(),
                contentDescription = "添付画像",
                modifier = Modifier
                  .padding(vertical = 4.dp)
                  .fillMaxWidth()
                  .heightIn(max = 300.dp),
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Fit
              )
            }
          }
        }
      }
    }
  }
}

/**
 * ポストの通常表示
 */
@Composable
private fun PostRowContent(
  displayText: String,
  columnInfo: ColumnInfo,
  visibleLines: Int,
  images: List<EmbedImagesViewImage>?,
  hasImages: Boolean
) {
  Row(
    modifier = Modifier
      .width(columnInfo.width.value)
      .padding(vertical = 4.dp, horizontal = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // テキスト部分
    Text(
      text = displayText,
      style = MaterialTheme.typography.body2,
      modifier = Modifier.weight(1f),
      maxLines = visibleLines,
      overflow = TextOverflow.Ellipsis
    )

    // 添付画像があれば右端に表示
    if (hasImages) {
      val firstImage = images?.firstOrNull()
      val thumbnail = firstImage?.thumb
      if (thumbnail != null) {
        Box(
          modifier = Modifier.padding(start = 8.dp)
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
              .data(thumbnail)
              .crossfade(true)
              .size(48 * 4)
              .build(),
            contentDescription = "サムネイル",
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(4.dp)),
            filterQuality = FilterQuality.High,
            contentScale = ContentScale.Crop
          )
        }
      }
    }
  }
}
