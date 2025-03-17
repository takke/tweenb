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
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.takke.tweenb.app.viewmodel.PostListViewModel

@Composable
fun PostListContent(
  modifier: Modifier = Modifier,
  viewModel: PostListViewModel = viewModel { PostListViewModel() },
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
        viewModel.columns.forEach {
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
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        items(viewModel.itemCount) { index ->
          PostItem(
            index = index,
            modifier = Modifier,
            cols = viewModel.columns,
          )
        }
      }

      // デモデータのロード
//      val coroutineScope = rememberCoroutineScope()
//      LaunchedEffect(Unit) {
//        viewModel.loadDemoData(coroutineScope, listState)
//      }
    }
  }
}