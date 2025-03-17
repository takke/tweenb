package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.takke.tweenb.app.domain.Account
import jp.takke.tweenb.app.domain.BlueskyAuthService
import jp.takke.tweenb.app.domain.BlueskyClient
import jp.takke.tweenb.app.domain.BsFeedViewPost
import jp.takke.tweenb.app.repository.AccountRepository
import jp.takke.tweenb.app.repository.AppPropertyRepository
import jp.takke.tweenb.app.repository.TimelineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class AppViewModel : ViewModel() {

  /**
   * UIState
   */
  data class UiState(
    val userName: String = "",
    val password: String = "",
    // エラーメッセージ
    val validationErrorMessage: String = "",
    // ログイン処理中
    val loading: Boolean = false,
    // ログイン状態
    val loginState: LoginState = LoginState.INIT,
    val code: String = "",
    // アカウントリスト
    val accounts: List<Account> = emptyList(),
    // タイムラインの投稿
    val timelinePosts: List<BsFeedViewPost> = emptyList(),
    // タイムライン読み込み中
    val timelineLoading: Boolean = false,
    // エラー情報
    val errorMessage: String = "",
    val errorStackTrace: String = "",
  ) {
    enum class LoginState {
      INIT,

      LOADING,

      // ブラウザを開いて認証コード入力待ち
      WAITING_CODE,
    }
  }

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  // アプリケーション設定リポジトリ
  private val appPropertyRepository = AppPropertyRepository.instance
  
  // アカウントリポジトリ
  private val accountRepository = AccountRepository.instance

  // Blueskyクライアント
  private val blueskyClient = BlueskyClient.create()

  // 認証サービス
  private val authService = BlueskyAuthService(accountRepository)

  // Blueskyクライアントの初期化状態
  var blueskyClientInitialized by mutableStateOf(blueskyClient.isInitialized())
    private set

  val account: Account?
    get() = blueskyClient.account

  // バージョン情報ダイアログの表示状態
  var showAboutDialog by mutableStateOf(false)
    private set

  // 設定ダイアログの表示状態
  var showConfigDialog by mutableStateOf(false)
    private set

  // 認証ダイアログの表示状態
  var showAuthDialog by mutableStateOf(false)
    private set

  // エラーダイアログの表示状態
  var showErrorDialog by mutableStateOf(false)
    private set

  // タブ関連の状態
  // TODO 他にも対応すること
//  val tabNames = listOf("Recent", "Notifications", "Lists")
  val tabNames = listOf("Recent")
  var selectedTabIndex by mutableStateOf(0)
    private set

  init {
    // 保存されているアカウント情報を読み込む
    loadAccounts()

    // アカウントがあれば先頭のものを選択
    if (_uiState.value.accounts.isNotEmpty()) {
      selectAccount(_uiState.value.accounts.first())
    }
  }

  /**
   * 保存されているアカウント情報を読み込む
   */
  private fun loadAccounts() {
    val accounts = accountRepository.getAccounts()
    println("accounts: ${accounts.size}")
    _uiState.update {
      it.copy(accounts = accounts)
    }
  }

  /**
   * アカウントを選択する
   */
  fun selectAccount(account: Account) {
    // Blueskyクライアントの初期化
    initializeBlueskyClient(account)
  }

  /**
   * Blueskyクライアントを初期化する
   */
  private fun initializeBlueskyClient(account: Account) {
    blueskyClient.initialize(account)

    // 初期化状態を更新
    blueskyClientInitialized = blueskyClient.isInitialized()
  }

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

  // 認証ダイアログの表示制御
  fun showAuthDialog() {
    showConfigDialog = false
    showAuthDialog = true
  }

  fun dismissAuthDialog() {
    showAuthDialog = false
  }

  // タブ選択制御
  fun selectTab(index: Int) {
    selectedTabIndex = index
  }

  /**
   * 現在選択中のタブを更新する
   */
  fun refreshCurrentTab() {
    viewModelScope.launch {
      try {
        println("タブ更新: ${tabNames[selectedTabIndex]}")

        // タブの種類に応じた更新処理
        when (selectedTabIndex) {
          0 -> refreshRecentTab()
          1 -> refreshNotificationsTab()
          2 -> refreshListsTab()
        }
      } catch (e: Exception) {
        println("タブ更新エラー: ${e.message}")
      }
    }
  }

  /**
   * Recentタブの更新
   */
  private suspend fun refreshRecentTab() {
    if (!blueskyClientInitialized) {
      println("Blueskyクライアントが初期化されていません")
      return
    }

    try {
      // タイムライン読み込み中フラグを設定
      _uiState.update {
        it.copy(timelineLoading = true)
      }

      // タイムラインを取得
      val timelineRepository = TimelineRepository.getInstance(blueskyClient)
      val posts = timelineRepository.getTimeline(limit = 30)
      println("タイムライン取得成功: ${posts.size}件")

      // UIStateを更新
      _uiState.update {
        it.copy(
          timelinePosts = posts,
          timelineLoading = false
        )
      }
    } catch (e: Exception) {
      println("タイムライン取得エラー: ${e.message}")
      e.printStackTrace()

      // エラー状態を設定
      _uiState.update {
        it.copy(timelineLoading = false)
      }

      // エラーダイアログを表示
      showErrorDialog("タイムラインの取得に失敗しました", e)
    }
  }

  /**
   * Notificationsタブの更新
   */
  private suspend fun refreshNotificationsTab() {
    // TODO: 通知の更新処理を実装
    // blueskyClient.getNotifications() など
  }

  /**
   * Listsタブの更新
   */
  private suspend fun refreshListsTab() {
    // TODO: リストの更新処理を実装
    // blueskyClient.getLists() など
  }

  // Blueskyクライアントの取得
  fun getBlueskyClient(): BlueskyClient {
    return blueskyClient
  }

  fun startAuth() {
    loginWithOAuth()
  }

  fun onCodeChanged(code: String) {
    _uiState.update {
      it.copy(code = code)
    }
  }

  fun onStartTokenRequest() {
    tokenRequest(uiState.value.code)
  }

  private fun loginWithOAuth() {
    viewModelScope.launch {
      val userName = _uiState.value.userName

      // OK
      _uiState.update {
        it.copy(validationErrorMessage = "")
      }

      try {
        _uiState.update {
          it.copy(loading = true, loginState = UiState.LoginState.LOADING)
        }

        // OAuth認証プロセスを開始
        authService.startOAuthProcess(userName)

        _uiState.update {
          it.copy(loginState = UiState.LoginState.WAITING_CODE)
        }
      } catch (e: Exception) {
        val message = authService.formatErrorMessage(e)
        _uiState.update {
          it.copy(validationErrorMessage = "エラーが発生しました: $message")
        }
      } finally {
        _uiState.update {
          it.copy(loading = false)
        }
      }
    }
  }

  private fun tokenRequest(code: String) {
    viewModelScope.launch {
      try {
        // トークン取得中
        _uiState.update {
          it.copy(loading = true, loginState = UiState.LoginState.LOADING)
        }

        // AccessToken とアカウント情報を取得する
        val account = authService.fetchTokenWithCode(code)
        if (account == null) {
          // 認証失敗
          _uiState.update {
            it.copy(
              validationErrorMessage = "認証に失敗しました",
              loginState = UiState.LoginState.INIT,
            )
          }
          return@launch
        }

        // アカウントリストを更新
        loadAccounts()

        // 新しく追加したアカウントを選択
        selectAccount(account)

        _uiState.update {
          it.copy(
            loginState = UiState.LoginState.INIT,
            code = "",
          )
        }

        dismissAuthDialog()

        // TODO 初期アクセス開始

      } catch (e: Exception) {
        val message = authService.formatErrorMessage(e)
        _uiState.update {
          it.copy(validationErrorMessage = "エラーが発生しました: $message")
        }
      } finally {
        _uiState.update {
          it.copy(loading = false)
        }
      }
    }
  }

  /**
   * エラーダイアログを表示する
   */
  fun showErrorDialog(message: String, exception: Exception) {
    val stackTrace = StringWriter().also { sw ->
      PrintWriter(sw).use { pw ->
        exception.printStackTrace(pw)
      }
    }.toString()

    _uiState.update {
      it.copy(
        errorMessage = message,
        errorStackTrace = stackTrace
      )
    }

    showErrorDialog = true
  }

  /**
   * エラーダイアログを閉じる
   */
  fun dismissErrorDialog() {
    showErrorDialog = false
  }
}
