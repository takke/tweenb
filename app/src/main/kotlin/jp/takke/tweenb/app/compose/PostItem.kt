package jp.takke.tweenb.app.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import jp.takke.tweenb.app.repository.AppPropertyRepository
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
  visibleLines: Int,
  tooltipEnabled: Boolean
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
                  .padding(4.dp)
              )
              val repostedBy = post.reason?.asReasonRepost?.by
              if (repostedBy != null) {
                Text(
                  text = "RP: @${repostedBy.handle}",
                  style = MaterialTheme.typography.caption,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                      // ユーザーをブラウザで開く
                      val url = repostedBy.url
                      openBrowser(url)
                    }
                    .padding(4.dp)
                )
              }
            }
          }

          ColumnType.Post -> {
            PostColumnContent(
              post = post,
              columnInfo = columnInfo,
              visibleLines = visibleLines,
              tooltipEnabled = tooltipEnabled
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
                .padding(4.dp)
                .clickable {
                  // ポストのURIを取得して、ブラウザで開く
                  val url = post.post.url
                  if (url != null) {
                    openBrowser(url)
                  }
                }
                .padding(4.dp)
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
  visibleLines: Int,
  tooltipEnabled: Boolean
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

  if (tooltipEnabled) {
    // ツールチップ表示有効
    TooltipArea(
      tooltip = {
        // ツールチップの内容（元のテキスト表示）
        PostTooltipContent(
          tooltipText = tooltipText,
          images = images,
          hasImages = hasImages,
          textSelectable = false,
          modifier = Modifier
            .padding(start = 64.dp)
        )
      },
      delayMillis = 500, // 表示までの遅延
      tooltipPlacement = TooltipPlacement.CursorPoint(
        alignment = Alignment.BottomStart
      )
    ) {
      // 通常表示の内容（空行削除済み）
      PostRowContent(displayText, columnInfo, visibleLines, images, hasImages)
    }
  } else {
    // ツールチップ表示無効

    // タップでツールチップ表示
    var showOverlayPopup by remember { mutableStateOf(false) }
    // タップ位置
    var tapPosition by remember { mutableStateOf(IntOffset(0, 0)) }
    // コンポーネントの位置情報
    var componentPosition by remember { mutableStateOf(IntOffset(0, 0)) }

    Box(
      modifier = Modifier
        .onGloballyPositioned { coordinates ->
          // コンポーネントのウィンドウ内での位置を記録
          componentPosition = IntOffset(
            coordinates.boundsInWindow().left.toInt(),
            coordinates.boundsInWindow().top.toInt()
          )
        }
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = { offset ->
              // ローカル座標をウィンドウ座標に変換
              val windowX = componentPosition.x + offset.x.toInt()
              val windowY = componentPosition.y + offset.y.toInt()
              // 変換した座標を記録
              tapPosition = IntOffset(windowX, windowY)
              println("tapPosition: $tapPosition, offset: $offset")
              // タップした位置でポップアップを表示
              showOverlayPopup = true
            },
          )
        }
    ) {
      PostRowContent(displayText, columnInfo, visibleLines, images, hasImages)
    }

    if (showOverlayPopup) {
      // オーバーレイ表示
      Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, 0),
        onDismissRequest = {
          showOverlayPopup = false
        },
        properties = PopupProperties(focusable = true),
        onPreviewKeyEvent = { false },
        onKeyEvent = { event ->
          // Escキーでオーバーレイを閉じる
          if (event.key == Key.Escape) {
            showOverlayPopup = false
            true
          } else {
            false
          }
        },
        content = {
          Box(
            Modifier
              .fillMaxSize()
          ) {
            // タップ検出用ダミーBox
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
                .clickableNoRipple {
                  // クリックでオーバーレイを閉じる
                  showOverlayPopup = false
                }
            )

            // ポップアップのサイズを測定してはみ出し防止
            var tooltipHeight by remember { mutableStateOf(0) }
            var tooltipWidth by remember { mutableStateOf(0) }

            // 画面サイズの取得
            val density = LocalDensity.current

            // 設定からウィンドウサイズを取得(本当はステータスバーのサイズ等も考慮すべき)
            val propertyRepository = AppPropertyRepository.instance
            val windowSize = propertyRepository.getWindowSize()
            val screenHeightPx = with(density) { windowSize.height.toPx() }.toInt()
            val screenWidthPx = with(density) { windowSize.width.toPx() }.toInt()

            // スクリーン下部からのオフセット計算（はみ出す場合は上に表示）
            val yBoundary = with(density) { 64.dp.toPx() }.toInt()
            val isOverflowBottom = tapPosition.y + tooltipHeight + yBoundary > screenHeightPx
            val yOffset = if (isOverflowBottom) {
              // 上に表示（タップ位置より上）
              tapPosition.y - tooltipHeight
            } else {
              // 下に表示（タップ位置より下）
              tapPosition.y - yBoundary
            }.coerceAtLeast(0) // 画面上部を超えないように

            // スクリーン右からのオフセット計算
            val xBoundary = with(density) { 96.dp.toPx() }.toInt()
            val isOverflowRight = tapPosition.x + tooltipWidth + xBoundary > screenWidthPx
            val xOffset = if (isOverflowRight) {
              // 左に寄せる
              (screenWidthPx - tooltipWidth - xBoundary).coerceAtLeast(0)
            } else {
              tapPosition.x - xBoundary
            }

            // サイズ計算が完了したかのフラグ
            var isTooltipSizeCalculated by remember { mutableStateOf(false) }

            Box(
              modifier = Modifier
                // サイズ計算が完了するまでは透過表示
                .alpha(if (isTooltipSizeCalculated) 1f else 0f)
            ) {
              PostTooltipContent(
                tooltipText = tooltipText,
                images = images,
                hasImages = hasImages,
                textSelectable = true,
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .offset(xOffset.dp, yOffset.dp)
                  .onSizeChanged {
                    // サイズ計算
                    if (it.height > 0 && it.width > 0) {
                      tooltipHeight = it.height
                      tooltipWidth = it.width
                      isTooltipSizeCalculated = true
                    }
                  }
              )
            }
          }
        }
      )
    }
  }
}

/**
 * ポストのツールチップ
 */
@Composable
private fun PostTooltipContent(
  tooltipText: String,
  images: List<EmbedImagesViewImage>?,
  hasImages: Boolean,
  textSelectable: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .padding(8.dp),
    shape = RoundedCornerShape(4.dp),
    elevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .padding(8.dp)
        .widthIn(max = 600.dp) // 最大幅を設定
    ) {
      // テキスト表示
      val content = @Composable {
        Text(
          text = tooltipText,
          style = MaterialTheme.typography.body2
        )
      }
      if (textSelectable) {
        // テキスト選択可能な場合はSelectionContainerでラップ
        SelectionContainer {
          content()
        }
      } else {
        // テキスト選択不可の場合は通常表示
        content()
      }

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
