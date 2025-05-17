package jp.takke.tweenb.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.takke.tweenb.app.compose.*
import jp.takke.tweenb.app.viewmodel.AppViewModel
import jp.takke.tweenb.app.viewmodel.PublishViewModel
import kotlin.system.exitProcess

@Composable
@Preview
fun FrameWindowScope.AppScreen() {
  val appViewModel = viewModel { AppViewModel() }
  val publishViewModel = viewModel { PublishViewModel(appViewModel.blueskyClient) }
  val uiState by appViewModel.uiState.collectAsState()
  val publishUiState by publishViewModel.uiState.collectAsState()

  MenuBar {
    Menu("ファイル") {
      Item("設定") {
        appViewModel.showConfigDialog()
      }
      Item("終了") {
        exitProcess(0)
      }
    }
    Menu("ヘルプ") {
      Item("バージョン情報") {
        appViewModel.showAboutDialog()
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
        tabNames = appViewModel.tabNames,
        selectedTabIndex = appViewModel.selectedTabIndex,
        onTabSelected = { index ->
          appViewModel.selectTab(index)
        },
        onRefresh = {
          appViewModel.refreshCurrentTab()
        },
        isLoading = uiState.timelineLoading
      )

      // 投稿入力欄
      PublishArea(
        initialText = publishUiState.currentInputText,
        onTextChange = { text ->
          publishViewModel.updateInputText(text)
        },
        onPost = { text ->
          publishViewModel.showPostConfirmation(text)
        }
      )

      // Status bar
      StatusBar(
        appViewModel = appViewModel,
        uiState = uiState
      )
    }

    // バージョン情報ダイアログ
    AboutDialog(
      appViewModel.showAboutDialog,
      onDismiss = { appViewModel.dismissAboutDialog() },
    )

    // 設定ダイアログ
    ConfigDialog(
      showConfigDialog = appViewModel.showConfigDialog,
      onDismiss = { appViewModel.dismissConfigDialog() },
      onShowAuthDialog = { appViewModel.showAuthDialog() },
      onDeleteAccount = { appViewModel.deleteCurrentAccount() },
      accountScreenName = appViewModel.account?.screenName,
      autoRefreshEnabled = uiState.autoRefreshEnabled,
      autoRefreshInterval = uiState.autoRefreshInterval,
      onAutoRefreshToggle = appViewModel::setAutoRefresh,
      onAutoRefreshIntervalChange = appViewModel::setAutoRefreshInterval,
      timelineVisibleLines = uiState.timelineVisibleLines,
      onTimelineVisibleLinesChange = appViewModel::setTimelineVisibleLines,
      tooltipEnabled = uiState.tooltipEnabled,
      onTooltipEnabledChange = appViewModel::setTooltipEnabled,
    )

    // 認証ダイアログ
    AuthDialog(
      showConfigDialog = appViewModel.showAuthDialog,
      onDismiss = { appViewModel.dismissAuthDialog() },
      onStartAuth = { appViewModel.startAuth() },
      uiState = uiState,
      onCodeChanged = appViewModel::onCodeChanged,
      onStartTokenRequest = appViewModel::onStartTokenRequest,
    )

    // エラーダイアログ
    ErrorDialog(
      show = appViewModel.showErrorDialog,
      errorMessage = uiState.errorMessage,
      stackTrace = uiState.errorStackTrace.replace("\t", "    "),
      onDismiss = { appViewModel.dismissErrorDialog() },
      onReAuth = {
        appViewModel.dismissErrorDialog()
        appViewModel.showAuthDialog(reAuth = true)
      },
    )

    // 投稿確認ダイアログ
    ConfirmPostDialog(
      show = publishViewModel.showPostConfirmDialog,
      postText = publishUiState.pendingPostText,
      onDismiss = { publishViewModel.cancelPostConfirmDialog() },
      onConfirm = {
        // 投稿処理
        appViewModel.processPost(execute = {
          return@processPost publishViewModel.createPost(publishUiState.pendingPostText)
        })

        publishViewModel.completePost()
      }
    )

    // 未認証ならすぐに認証画面を開く
    // 認証済みなら更新実行
    LaunchedEffect(Unit) {
      if (!appViewModel.blueskyClientInitialized) {
        // 未認証
        appViewModel.showAuthDialog()
      } else {
        // 認証済み
        appViewModel.refreshCurrentTab()
      }
    }
  }
}

