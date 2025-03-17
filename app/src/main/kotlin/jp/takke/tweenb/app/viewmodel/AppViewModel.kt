package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import jp.takke.tweenb.app.domain.BlueskyClient

class AppViewModel : ViewModel() {
  // Blueskyクライアント
  private val blueskyClient = BlueskyClient.create()

  // Blueskyクライアントの初期化状態
  var blueskyClientInitialized by mutableStateOf(blueskyClient.isInitialized())
    private set

  // バージョン情報ダイアログの表示状態
  var showAboutDialog by mutableStateOf(false)
    private set

  // 設定ダイアログの表示状態
  var showConfigDialog by mutableStateOf(false)
    private set

  // タブ関連の状態
  val tabNames = listOf("Recent", "Notifications", "Lists")
  var selectedTabIndex by mutableStateOf(0)
    private set

  // バージョン情報ダイアログの表示制御
  fun showAboutDialog() {
    showAboutDialog = true
  }

  fun dismissAboutDialog() {
    showAboutDialog = false
  }

  // 設定ダイアログの表示制御
  fun showConfigDialog() {
    showConfigDialog = true
  }

  fun dismissConfigDialog() {
    showConfigDialog = false
  }

  // タブ選択制御
  fun selectTab(index: Int) {
    selectedTabIndex = index
  }

  // Blueskyクライアントの取得
  fun getBlueskyClient(): BlueskyClient {
    return blueskyClient
  }
} 