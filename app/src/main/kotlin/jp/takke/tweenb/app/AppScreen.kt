package jp.takke.tweenb.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import jp.takke.tweenb.app.compose.AboutDialog
import jp.takke.tweenb.app.compose.ConfigDialog
import jp.takke.tweenb.app.compose.PostListContent
import jp.takke.tweenb.app.compose.Tab
import jp.takke.tweenb.app.viewmodel.AppViewModel
import kotlin.system.exitProcess

@Composable
@Preview
fun FrameWindowScope.AppScreen() {
  // ViewModelのインスタンス化
  val viewModel = remember { AppViewModel() }

  MenuBar {
    Menu("ファイル") {
      Item("設定") {
        viewModel.showConfigDialog()
      }
      Item("終了") {
        exitProcess(0)
      }
    }
    Menu("ヘルプ") {
      Item("バージョン情報") {
        viewModel.showAboutDialog()
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
      Tab(
        tabNames = viewModel.tabNames,
        selectedTabIndex = viewModel.selectedTabIndex,
        onTabSelected = { index ->
          viewModel.selectTab(index)
        },
      )

      // Status bar
      val statusText = if (viewModel.blueskyClientInitialized) {
        "Bluesky API: 接続済み"
      } else {
        "Bluesky API: 未接続"
      }
      Text(
        text = statusText,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.LightGray)
          .padding(8.dp)
      )
    }

    // バージョン情報ダイアログ
    AboutDialog(
      viewModel.showAboutDialog,
      onDismiss = { viewModel.dismissAboutDialog() },
    )

    // 設定ダイアログ
    ConfigDialog(
      viewModel.showConfigDialog,
      onDismiss = { viewModel.dismissConfigDialog() },
    )
  }
}

