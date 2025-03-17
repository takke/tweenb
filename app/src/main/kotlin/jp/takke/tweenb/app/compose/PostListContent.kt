package jp.takke.tweenb.app.compose

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.repository.AppPropertyRepository
import jp.takke.tweenb.app.viewmodel.AppViewModel
import java.awt.Cursor

@Composable
fun PostListContent(
  modifier: Modifier = Modifier,
  appViewModel: AppViewModel = viewModel(),
) {
  // プロパティリポジトリ
  val propertyRepository = remember { AppPropertyRepository.instance }

  val uiState by appViewModel.uiState.collectAsState()

  // カラム定義（プロパティから読み込む）
  val columns = uiState.columns

  // カラム情報が変更されたら保存する
  DisposableEffect(Unit) {
    onDispose {
      // アプリ終了時にカラム情報を保存
      propertyRepository.saveColumns(columns)
    }
  }

  Box(
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)
    ) {
      // Header
      PostHeaders(columns, propertyRepository)

      // Post items
      val listState = rememberLazyListState()

      if (uiState.timelinePosts.isEmpty() && appViewModel.selectedTabIndex == 0) {
        // 投稿がない場合
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "投稿がありません",
            style = MaterialTheme.typography.body1
          )
        }
      } else {
        // 投稿リスト
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {

          LaunchedEffect(uiState.timelinePosts) {
            // 新しいデータが追加されたのでスクロール位置を末尾にする
            // TODO より細かい制御を行うこと
            if (uiState.timelinePosts.isNotEmpty()) {
              listState.animateScrollToItem(uiState.timelinePosts.size - 1)
            }
          }

          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
          ) {
            when (appViewModel.selectedTabIndex) {
              0 -> {
                // Recentタブ
                items(uiState.timelinePosts) { post ->
                  PostItem(
                    post = post,
                    modifier = Modifier,
                    columns = columns,
                  )
                }
              }

              else -> {
                // その他のタブ（未実装）
                item {
                  Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "このタブは未実装です",
                      style = MaterialTheme.typography.body1
                    )
                  }
                }
              }
            }
          }

          // スクロールバー
          VerticalScrollbar(
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(listState)
          )
        }
      }
    }
  }
}

@Composable
private fun PostHeaders(
  columns: List<ColumnInfo>,
  propertyRepository: AppPropertyRepository
) {
  var headerHeight by remember { mutableStateOf(0.dp) }
  val headerBorderColor = Color.LightGray
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .onSizeChanged {
        headerHeight = it.height.dp
      }
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = Color.LightGray,
        shape = RoundedCornerShape(4.dp),
      )
  ) {
    columns.forEachIndexed { index, columnInfo ->
      Text(
        text = columnInfo.name,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
          .width(columnInfo.width.value - if (index == 0) 2.dp else 5.dp)
          .padding(6.dp)
      )

      // 区切り線（ドラッグ可能）
      ResizableColumnDivider(
        headerHeight = headerHeight,
        headerBorderColor = headerBorderColor,
        index = index,
        columns = columns,
        propertyRepository = propertyRepository,
      )
    }
  }
}

@Composable
private fun ResizableColumnDivider(
  headerHeight: Dp,
  headerBorderColor: Color,
  index: Int,
  columns: List<ColumnInfo>,
  propertyRepository: AppPropertyRepository
) {
  // 水平リサイズカーソルを作成
  val resizeCursor = remember { PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)) }

  Box(
    modifier = Modifier
      .width(6.dp)
      .height(headerHeight)
      .pointerHoverIcon(resizeCursor)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            // ドラッグ終了時にカラム情報を保存
            propertyRepository.saveColumns(columns)
          }
        ) { change, dragAmount ->
          change.consume()
          // 現在の列の幅を調整
          val currentWidth = columns[index].width.value
          val newWidth = (currentWidth + dragAmount.x.toDp()).coerceAtLeast(40.dp)

          columns[index].width.value = newWidth
        }
      }
  ) {
    VerticalDivider(
      height = headerHeight,
      color = headerBorderColor,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}