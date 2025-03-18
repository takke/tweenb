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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.takke.tweenb.app.compose.*
import jp.takke.tweenb.app.viewmodel.AppViewModel
import kotlin.system.exitProcess

@Composable
@Preview
fun FrameWindowScope.AppScreen() {
  val viewModel = viewModel { AppViewModel() }
  val uiState by viewModel.uiState.collectAsState()

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
        onRefresh = {
          viewModel.refreshCurrentTab()
        },
        isLoading = uiState.timelineLoading
      )

      // Status bar
      val statusText = if (viewModel.blueskyClientInitialized) {
        "@${viewModel.account?.screenName}"
      } else {
        "未認証"
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
      showConfigDialog = viewModel.showConfigDialog,
      onDismiss = { viewModel.dismissConfigDialog() },
      onShowAuthDialog = { viewModel.showAuthDialog() },
      onDeleteAccount = { viewModel.deleteCurrentAccount() },
      accountScreenName = viewModel.account?.screenName,
      autoRefreshEnabled = uiState.autoRefreshEnabled,
      autoRefreshInterval = uiState.autoRefreshInterval,
      onAutoRefreshToggle = viewModel::toggleAutoRefresh,
      onAutoRefreshIntervalChange = viewModel::setAutoRefreshInterval,
    )

    // 認証ダイアログ
    AuthDialog(
      showConfigDialog = viewModel.showAuthDialog,
      onDismiss = { viewModel.dismissAuthDialog() },
      onStartAuth = { viewModel.startAuth() },
      uiState = uiState,
      onCodeChanged = viewModel::onCodeChanged,
      onStartTokenRequest = viewModel::onStartTokenRequest,
    )

    // エラーダイアログ
    ErrorDialog(
      show = viewModel.showErrorDialog,
      errorMessage = uiState.errorMessage,
      stackTrace = uiState.errorStackTrace.replace("\t", "    "),
      onDismiss = { viewModel.dismissErrorDialog() },
      onReAuth = {
        viewModel.dismissErrorDialog()
        viewModel.showAuthDialog(reAuth = true)
      },
    )

    // 未認証ならすぐに認証画面を開く
    // 認証済みなら更新実行
    LaunchedEffect(Unit) {
      if (!viewModel.blueskyClientInitialized) {
        // 未認証
        viewModel.showAuthDialog()
      } else {
        // 認証済み
        viewModel.refreshCurrentTab()
      }
    }
  }
}

