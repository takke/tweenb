package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.domain.BlueskyClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.ATProtocolException
import work.socialhub.kbsky.auth.AuthFactory
import work.socialhub.kbsky.auth.OAuthContext
import work.socialhub.kbsky.auth.api.entity.oauth.BuildAuthorizationUrlRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthPushedAuthorizationRequest
import work.socialhub.kbsky.domain.Service
import java.awt.Desktop
import java.net.URI

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

  // 認証ダイアログの表示状態
  var showAuthDialog by mutableStateOf(false)
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

  private fun loginWithOAuth() {

    viewModelScope.launch {

      // TODO dummyなので書き換えること
//      _uiState.update {
//        it.copy(userName = "takke.jp")
//      }
      val userName = _uiState.value.userName

      // 簡易バリデーション
//      if (userName.isEmpty()) {
//        // ユーザー名が空
//        _uiState.update {
//          it.copy(validationErrorMessage = "ユーザー名を入力してください")
//        }
//        return@launch
//      }

      // OK
      _uiState.update {
        it.copy(validationErrorMessage = "")
      }

      // OAuth ログイン開始
      val oauthContext = OAuthContext().also {
        it.clientId = AppConstants.OAUTH_CLIENT_ID
        it.redirectUri = AppConstants.CALLBACK_URL
      }

      val loginHint = userName
      loading {
        withContext(Dispatchers.IO) {
          _uiState.update {
            it.copy(loginState = UiState.LoginState.LOADING)
          }

          val response = AuthFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .oauth()
            .pushedAuthorizationRequest(
              oauthContext,
              OAuthPushedAuthorizationRequest().also {
                it.loginHint = loginHint
              }
            )

//          logger.dd { "response: ${response.data.requestUri}" }

          val authorizeUrl = AuthFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .oauth()
            .buildAuthorizationUrl(
              oauthContext,
              BuildAuthorizationUrlRequest().also {
                it.requestUri = response.data.requestUri
              }
            )

//          logger.dd { "authorizeUrl: $authorizeUrl" }
          println("authorizeUrl: $authorizeUrl")

          // ブラウザを開く
          Desktop.getDesktop().browse(URI(authorizeUrl))

          _uiState.update {
            it.copy(loginState = UiState.LoginState.WAITING_CODE)
          }
        }
      }
    }
  }

  /**
   * ローディング表示
   */
  private suspend fun loading(function: suspend () -> Unit) {
    try {
      _uiState.update {
        it.copy(loading = true)
      }

      function()
    } catch (e: Exception) {

      // TODO Formatterを導入すること
      val message = if (e is ATProtocolException) {
        "$e\n${e.body}\n${e.response}\n${e.cause}"
      } else {
        e.toString()
      }

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