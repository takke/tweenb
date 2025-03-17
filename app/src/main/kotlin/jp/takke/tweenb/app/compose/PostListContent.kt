package jp.takke.tweenb.app.compose

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import jp.takke.tweenb.app.viewmodel.AppViewModel

@Composable
fun PostListContent(
  modifier: Modifier = Modifier,
  appViewModel: AppViewModel = viewModel(),
) {
  // カラム定義
  val columns = listOf(
    ColumnInfo(ColumnType.Icon, "", 64.dp),
    ColumnInfo(ColumnType.Name, "名前", 120.dp),
    ColumnInfo(ColumnType.Post, "投稿", 360.dp),
    ColumnInfo(ColumnType.DateTime, "日時", 120.dp),
  )

  val uiState by appViewModel.uiState.collectAsState()

  Box(
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)
    ) {
      // Header
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
        columns.forEach {
          Text(
            text = it.name,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
              .width(it.width)
              .padding(8.dp)
          )
          VerticalDivider(
            height = headerHeight,
            color = headerBorderColor,
          )
        }
      }

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