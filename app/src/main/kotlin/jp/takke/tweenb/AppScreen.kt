package jp.takke.tweenb

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
@Preview
fun AppScreen() {

  MaterialTheme {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      // Menu

      // TODO

      // Content
      PostListContent(
        modifier = Modifier.weight(1f)
      )

      // タブ
      val tabNames = listOf(
        "Recent", "Notifications", "Lists",
      )
      var selectedTabIndex by remember { mutableStateOf(0) }
      Tab(
        tabNames = tabNames,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index ->
          selectedTabIndex = index
        },
      )

      // Status bar
      // TODO
      Text(
        text = "ステータスバーとか",
        style = MaterialTheme.typography.body1,
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.LightGray)
          .padding(8.dp)
      )
    }
  }
}

enum class ColumnType {
  Icon,
  Name,
  Post,
  DateTime,
}

data class ColumnInfo(
  val type: ColumnType,
  val name: String,
  val width: Dp,
)

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

@Composable
fun PostItem(
  index: Int,
  modifier: Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .drawWithContent {
          drawContent()
          drawLine(
            color = Color.LightGray,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 1.dp.toPx()
          )
          drawLine(
            color = Color.LightGray,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
          )
        }
    ) {
      cols.forEach {
        Text(
          text = "${index + 1} ${it.name}",
          style = MaterialTheme.typography.body1,
          modifier = Modifier
            .width(it.width)
            .padding(8.dp)
        )

        VerticalDivider(
          height = 48.dp,
          color = Color.LightGray,
        )
      }
    }

    Divider(
      color = Color.LightGray
    )
  }
}
