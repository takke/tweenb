package jp.takke.tweenb.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import jp.takke.tweenb.app.compose.AboutDialog
import jp.takke.tweenb.app.compose.ConfigDialog
import jp.takke.tweenb.app.compose.PostListContent
import jp.takke.tweenb.app.compose.Tab
import kotlin.system.exitProcess

@Composable
@Preview
fun FrameWindowScope.AppScreen() {
  // バージョン情報ダイアログの表示状態
  var showAboutDialog by remember { mutableStateOf(false) }
  // 設定ダイアログの表示状態
  var showConfigDialog by remember { mutableStateOf(false) }

  MenuBar {
    Menu("ファイル") {
      Item("設定") {
        showConfigDialog = true
      }
      Item("終了") {
        exitProcess(0)
      }
    }
    Menu("ヘルプ") {
      Item("バージョン情報") {
        showAboutDialog = true
      }
    }
  }

  MaterialTheme {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {

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

    // バージョン情報ダイアログ
    AboutDialog(
      showAboutDialog,
      onDismiss = { showAboutDialog = false },
    )

    // 設定ダイアログ
    ConfigDialog(
      showConfigDialog,
      onDismiss = { showConfigDialog = false },
    )
  }
}

