package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.domain.Account
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import jp.takke.tweenb.app.repository.*
import jp.takke.tweenb.app.service.BlueskyAuthService
import jp.takke.tweenb.app.util.BsFeedViewPost
import jp.takke.tweenb.app.util.LoggerWrapper
import jp.takke.tweenb.app.util.key
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class AppViewModel : ViewModel() {

  /**
   * UIState
   */
  data class UiState(
    val userName: String = "",
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
    // カラム情報
    val columns: List<ColumnInfo> = emptyList(),
    // 自動更新設定
    val autoRefreshEnabled: Boolean = false,
    val autoRefreshInterval: Int = AppConstants.DEFAULT_AUTO_REFRESH_INTERVAL,
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
  val tabNames = listOf("Recent", "Notifications", "Lists")
  var selectedTabIndex by mutableStateOf(0)
    private set

  private val logger = LoggerWrapper("AppViewModel")

  // アプリプロパティ
  private val propertyRepository = AppPropertyRepository.instance

  // 自動更新用のジョブ
  private var autoRefreshJob: Job? = null

  // 投稿確認ダイアログの表示状態
  var showPostConfirmDialog by mutableStateOf(false)
    private set

  // 投稿確認中のテキスト
  var pendingPostText by mutableStateOf("")
    private set

  // 入力中のテキスト
  var currentInputText by mutableStateOf("")
    private set

  init {
    // 保存されているアカウント情報を読み込む
    loadAccounts()

    // アカウントがあれば先頭のものを選択
    if (_uiState.value.accounts.isNotEmpty()) {
      selectAccount(_uiState.value.accounts.first())
    }

    // 保存されたカラム情報をロード
    val propertyRepository = AppPropertyRepository.instance
    val columnsFromProp = propertyRepository.getColumns()
    logger.d("getColumns: $columnsFromProp")
    _uiState.update {
      it.copy(columns = if (columnsFromProp.isNullOrEmpty()) createDefaultColumns() else columnsFromProp)
    }

    // 自動更新設定をロード
    loadAutoRefreshSettings()
  }

  /**
   * デフォルトのカラム情報を作成する
   */
  private fun createDefaultColumns(): List<ColumnInfo> {
    return listOf(
      ColumnInfo(
        type = ColumnType.Icon,
        name = "",
        initialWidth = 64.dp,
      ),
      ColumnInfo(
        type = ColumnType.Name,
        name = "名前",
        initialWidth = 120.dp
      ),
      ColumnInfo(
        type = ColumnType.Post,
        name = "投稿",
        initialWidth = 540.dp
      ),
      ColumnInfo(
        type = ColumnType.DateTime,
        name = "日時",
        initialWidth = 96.dp
      ),
      ColumnInfo(
        type = ColumnType.ScreenName,
        name = "ユーザ名",
        initialWidth = 120.dp
      ),
    )
  }

  /**
   * 保存されているアカウント情報を読み込む
   */
  private fun loadAccounts() {
    val accounts = accountRepository.getAccounts()
    logger.i("accounts: ${accounts.size}")
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
  fun showAuthDialog(reAuth: Boolean = false) {
    showConfigDialog = false
    showAuthDialog = true

    _uiState.update {
      it.copy(
        userName = if (reAuth) {
          account?.screenName ?: ""
        } else {
          ""
        }
      )
    }
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
        logger.i("タブ更新: ${tabNames[selectedTabIndex]}")

        // タブの種類に応じた更新処理
        when (selectedTabIndex) {
          0 -> refreshRecentTab()
          1 -> refreshNotificationsTab()
          2 -> refreshListsTab()
        }
      } catch (e: Exception) {
        logger.e("タブ更新エラー: ${e.message}", e)
      }
    }
  }

  /**
   * Recentタブの更新
   */
  private suspend fun refreshRecentTab() {
    if (!blueskyClientInitialized) {
      logger.w("Blueskyクライアントが初期化されていません")
      return
    }

    try {
      // タイムライン読み込み中フラグを設定
      _uiState.update {
        it.copy(timelineLoading = true)
      }

      // タイムラインを取得
      val timelineRepository = TimelineRepository.getInstance(blueskyClient)
      val newPosts = timelineRepository.getTimeline(limit = 30)
      logger.i("タイムライン取得成功: ${newPosts.size}件")

      // 既存のデータと新しいデータをマージ
      // 投稿のURIをキーとして使用し、重複を排除
      val existingPosts = _uiState.value.timelinePosts
      val existingKeys = existingPosts.map { it.key }.toSet()

      // 既存のデータにない新しい投稿だけをフィルタリング
      val uniqueNewPosts = newPosts.filter { post -> post.key !in existingKeys }

      // 既存の投稿と新しい投稿をマージ
      val mergedPosts = existingPosts + uniqueNewPosts.reversed()

      logger.i("マージ後のタイムライン: ${mergedPosts.size}件（新規追加: ${uniqueNewPosts.size}件）")

      // UIStateを更新
      _uiState.update {
        it.copy(
          timelinePosts = mergedPosts,
          timelineLoading = false
        )
      }
    } catch (e: Exception) {
      logger.e("タイムライン取得エラー: ${e.message}", e)

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
  @Suppress("RedundantSuspendModifier")
  private suspend fun refreshNotificationsTab() {
    // TODO: 通知の更新処理を実装
    // blueskyClient.getNotifications() など
  }

  /**
   * Listsタブの更新
   */
  @Suppress("RedundantSuspendModifier")
  private suspend fun refreshListsTab() {
    // TODO: リストの更新処理を実装
    // blueskyClient.getLists() など
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
      logger.i("認証開始: $userName")

      // OK
      _uiState.update {
        it.copy(
          loading = true,
          loginState = UiState.LoginState.LOADING,
          validationErrorMessage = ""
        )
      }

      try {
        // 認証URLを取得
        val authUrl = authService.startOAuthProcess(userName)
        logger.d("認証URL: $authUrl")

        // 認証コード入力待ち
        _uiState.update {
          it.copy(
            loginState = UiState.LoginState.WAITING_CODE
          )
        }
      } catch (e: Exception) {
        logger.e("認証エラー: ${e.message}", e)

        // エラー状態を設定
        _uiState.update {
          it.copy(
            loginState = UiState.LoginState.INIT,
            validationErrorMessage = authService.formatErrorMessage(e)
          )
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

        // 初期アクセス開始
        refreshCurrentTab()

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
  fun showErrorDialog(message: String, exception: Exception?) {
    val stackTrace = StringWriter().also { sw ->
      if (exception != null) {
        PrintWriter(sw).use { pw ->
          exception.printStackTrace(pw)
        }
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

  /**
   * アカウントを削除する
   */
  fun deleteCurrentAccount() {
    // 現在のアカウントがあれば削除処理を実行
    val currentAccount = account
    if (currentAccount != null) {
      val accountId = currentAccount.accountId
      if (accountRepository.deleteAccount(accountId)) {
        // アカウントリストを再読み込み
        loadAccounts()

        // Blueskyクライアントの初期化状態をリセット
        blueskyClientInitialized = false

        // 設定ダイアログを閉じる
        dismissConfigDialog()

        // アカウントリストに残りがあれば、先頭のアカウントを選択
        if (_uiState.value.accounts.isNotEmpty()) {
          selectAccount(_uiState.value.accounts.first())
        } else {
          // アカウントがなくなったら認証ダイアログを表示
          showAuthDialog()
        }
      }
    }
  }

  /**
   * 自動更新設定をロードする
   */
  private fun loadAutoRefreshSettings() {
    val enabled = propertyRepository.isAutoRefreshEnabled()
    val interval = propertyRepository.getAutoRefreshInterval()

    _uiState.update {
      it.copy(
        autoRefreshEnabled = enabled,
        autoRefreshInterval = interval
      )
    }

    // 自動更新が有効なら開始
    if (enabled) {
      startAutoRefresh()
    }
  }

  /**
   * 自動更新の有効/無効を切り替える
   */
  fun toggleAutoRefresh(enabled: Boolean) {
    _uiState.update {
      it.copy(autoRefreshEnabled = enabled)
    }

    propertyRepository.setAutoRefreshEnabled(enabled)

    if (enabled) {
      startAutoRefresh()
    } else {
      stopAutoRefresh()
    }
  }

  /**
   * 自動更新間隔を設定する
   */
  fun setAutoRefreshInterval(intervalSeconds: Int) {
    _uiState.update {
      it.copy(autoRefreshInterval = intervalSeconds)
    }

    propertyRepository.setAutoRefreshInterval(intervalSeconds)

    // 自動更新が有効なら再開
    if (uiState.value.autoRefreshEnabled) {
      restartAutoRefresh()
    }
  }

  /**
   * 自動更新を開始する
   */
  private fun startAutoRefresh() {
    // すでに実行中なら何もしない
    if (autoRefreshJob != null && autoRefreshJob?.isActive == true) {
      return
    }

    autoRefreshJob = viewModelScope.launch {
      while (isActive) {
        val interval = uiState.value.autoRefreshInterval * 1000L
        delay(interval)

        logger.i("自動更新を実行します (間隔: ${uiState.value.autoRefreshInterval}秒)")
        refreshCurrentTab()
      }
    }
  }

  /**
   * 自動更新を停止する
   */
  private fun stopAutoRefresh() {
    autoRefreshJob?.cancel()
    autoRefreshJob = null
  }

  /**
   * 自動更新を再開する
   */
  private fun restartAutoRefresh() {
    stopAutoRefresh()
    startAutoRefresh()
  }

  /**
   * 投稿テキストを更新する
   */
  fun updateInputText(text: String) {
    currentInputText = text
  }

  /**
   * 投稿前の確認ダイアログを表示する
   */
  fun showPostConfirmation(text: String) {
    if (!blueskyClientInitialized) {
      logger.w("Blueskyクライアントが初期化されていません")
      showErrorDialog("Blueskyに接続されていません", null)
      return
    }

    pendingPostText = text
    showPostConfirmDialog = true
  }

  /**
   * 投稿確認ダイアログを閉じる
   */
  fun dismissPostConfirmDialog() {
    showPostConfirmDialog = false
    pendingPostText = ""
    // キャンセル時は入力テキストを元に戻さない（そのままの状態を維持）
  }

  /**
   * 投稿確認ダイアログをキャンセルする
   * キャンセル時は入力欄を元に戻す
   */
  fun cancelPostConfirmDialog() {
    showPostConfirmDialog = false
    pendingPostText = ""
    // 現在のテキストを保持
  }

  /**
   * 投稿完了後の処理
   */
  fun completePost() {
    // 投稿完了後、入力欄をクリア
    currentInputText = ""
    dismissPostConfirmDialog()
  }

  /**
   * 投稿を作成する
   */
  fun createPost(text: String) {
    if (!blueskyClientInitialized) {
      logger.w("Blueskyクライアントが初期化されていません")
      showErrorDialog("Blueskyに接続されていません", null)
      return
    }

    // 投稿処理を実行
    viewModelScope.launch {
      try {
        // 投稿開始
        logger.i("投稿開始: $text")

        _uiState.update {
          it.copy(timelineLoading = true)
        }

        // 投稿処理
        val postRepository = PostRepository.getInstance(blueskyClient)
        val success = postRepository.createPost(text)

        if (success) {
          logger.i("投稿成功")

          // 投稿成功後、タイムラインを更新
          refreshCurrentTab()
        } else {
          logger.e("投稿失敗")
          showErrorDialog("投稿に失敗しました", null)

          _uiState.update {
            it.copy(timelineLoading = false)
          }
        }
      } catch (e: Exception) {
        logger.e("投稿エラー: ${e.message}", e)
        showErrorDialog("投稿に失敗しました", e)

        _uiState.update {
          it.copy(timelineLoading = false)
        }
      }
    }
  }

}
