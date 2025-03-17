package jp.takke.tweenb.app.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostListViewModel : ViewModel() {
  // カラム定義
  val columns = listOf(
    ColumnInfo(ColumnType.Icon, "", 64.dp),
    ColumnInfo(ColumnType.Name, "名前", 120.dp),
    ColumnInfo(ColumnType.Post, "投稿", 360.dp),
    ColumnInfo(ColumnType.DateTime, "日時", 120.dp),
  )

  // 投稿アイテム数
  var itemCount by mutableStateOf(5)
    private set

  // デモ用のデータロード処理
//  fun loadDemoData(coroutineScope: CoroutineScope, listState: LazyListState) {
//    coroutineScope.launch {
//      delay(2_000)
//      println("update")
//
//      repeat(50) {
//        itemCount++
//        listState.scrollToItem(itemCount - 1)
//        delay(500)
//      }
//    }
//  }
} 