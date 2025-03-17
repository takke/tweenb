package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.takke.tweenb.app.domain.BlueskyClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

  fun startAuth() {
    loginWithOAuth()
  }

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
  )

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private fun loginWithOAuth() {

    viewModelScope.launch {

      _uiState.update {
        it.copy(loading = true)
      }
      // TODO dummyなので書き換えること
//      _uiState.update {
//        it.copy(userName = "takke.jp"
//      }
      val userName = _uiState.value.userName

      // 簡易バリデーション
      if (userName.isEmpty()) {
        // ユーザー名が空
        _uiState.update {
          it.copy(validationErrorMessage = "ユーザー名を入力してください")
        }
        return@launch
      }

      // OK
//      _uiState.update {
//        it.copy(validationErrorMessage = "")
//      }
//
//      // OAuth ログイン開始
//      val oauthContext = OAuthContext().also {
//        it.clientId = flavorConstants.blueskyOAuthClientId
//        it.redirectUri = flavorConstants.blueskyCallbackUrl
//      }
//
//      val loginHint = state.userNameOrMailAddress
//      loading {
//        withContext(Dispatchers.Default) {
//          val response = AuthFactory
//            .instance(BSKY_SOCIAL.uri)
//            .oauth()
//            .pushedAuthorizationRequest(
//              oauthContext,
//              OAuthPushedAuthorizationRequest().also {
//                it.loginHint = loginHint
//              }
//            )
//
//          logger.dd { "response: ${response.data.requestUri}" }
//
//          val authorizeUrl = AuthFactory
//            .instance(BSKY_SOCIAL.uri)
//            .oauth()
//            .buildAuthorizationUrl(
//              oauthContext,
//              BuildAuthorizationUrlRequest().also {
//                it.requestUri = response.data.requestUri
//              }
//            )
//
//          logger.dd { "authorizeUrl: $authorizeUrl" }
//
//          // OAuth アクセストークン取得に必要なので oauthContext を保存しておく
//          BlueskyLoginOAuthContextRepository(logger).save(oauthContext)
//
//          // ブラウザを開く
//          _showBrowserEvent.emit(authorizeUrl)
//        }
//      }
//    }
    }
  }
}