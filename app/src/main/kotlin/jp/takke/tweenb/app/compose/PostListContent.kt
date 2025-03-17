package jp.takke.tweenb.app.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import kotlinx.coroutines.delay

// TODO 設定に保存し、カスタマイズできるようにすること
val cols = listOf(
  ColumnInfo(ColumnType.Icon, "", 64.dp),
  ColumnInfo(ColumnType.Name, "名前", 120.dp),
  ColumnInfo(ColumnType.Post, "投稿", 360.dp),
  ColumnInfo(ColumnType.DateTime, "日時", 120.dp),
)

@Composable
fun PostListContent(
  modifier: Modifier = Modifier,
) {
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
        cols.forEach {
          Text(
            text = it.name,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
              .width(it.width)
              .padding(8.dp)
          )
//          if (it.type != ColumnType.Icon) {
          VerticalDivider(
            height = headerHeight,
            color = headerBorderColor,
          )
//          }
        }

      }

      // Post items
      var itemCount by remember { mutableStateOf(5) }
      val listState = rememberLazyListState()
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        items(itemCount) { index ->
          PostItem(
            index = index,
            modifier = Modifier,
          )
        }
      }

      LaunchedEffect(Unit) {
        delay(2_000)
        println("update")

        repeat(50) {
          itemCount++
          listState.scrollToItem(itemCount - 1)
          delay(500)
        }
      }

    }
  }

}