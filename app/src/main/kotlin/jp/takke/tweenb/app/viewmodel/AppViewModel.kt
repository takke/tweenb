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
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.actor.ActorGetProfileRequest
import work.socialhub.kbsky.api.entity.app.bsky.actor.ActorGetProfileResponse
import work.socialhub.kbsky.auth.AuthFactory
import work.socialhub.kbsky.auth.AuthProvider
import work.socialhub.kbsky.auth.OAuthContext
import work.socialhub.kbsky.auth.OAuthProvider
import work.socialhub.kbsky.auth.api.entity.oauth.BuildAuthorizationUrlRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthAuthorizationCodeTokenRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthPushedAuthorizationRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthTokenResponse
import work.socialhub.kbsky.domain.Service
import work.socialhub.kbsky.domain.Service.BSKY_SOCIAL
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

  // OAuthコンテキスト
  var oauthContext: OAuthContext? = null

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

  fun onStartTokenRequest() {
    tokenRequest(uiState.value.code)
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
      oauthContext = OAuthContext().also {
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
              oauthContext!!,
              OAuthPushedAuthorizationRequest().also {
                it.loginHint = loginHint
              }
            )

//          logger.dd { "response: ${response.data.requestUri}" }

          val authorizeUrl = AuthFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .oauth()
            .buildAuthorizationUrl(
              oauthContext!!,
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

  private fun tokenRequest(code: String) {
    viewModelScope.launch {
      // TODO 実装すること

      // トークン取得中
      _uiState.update {
        it.copy(loginState = UiState.LoginState.LOADING)
      }

      // AccessToken とアカウント情報を取得する
      val pair = fetchAccessTokenAndAccountInfoAsync(code)
      if (pair == null) {
        // 認証失敗
        _uiState.update {
          it.copy(
            validationErrorMessage = "認証に失敗しました",
            loginState = UiState.LoginState.INIT,
          )
        }
        return@launch
      }

      val (response, user) = pair

      // 追加の場合はアカウント数上限をチェックする
      println("user.did[$user.did]")

      // TODO 永続化
//      // "did:plc:mm5f..." のような文字列
//      accountId = user.did,
//      // handle
//      screenNameWIN = user.handle,
//      // accessJwt
//      accessJwt = response.accessToken,
//      refreshJwt = response.refreshToken,
//      dPoPNonce = oauthContext.dPoPNonce,
//      publicKey = oauthContext.publicKey ?: "",
//      privateKey = oauthContext.privateKey ?: "",

      _uiState.update {
        it.copy(
          loginState = UiState.LoginState.INIT,
          code = "",
        )
      }

      // Blueskyクライアントの初期化
      // TODO 実装すること

      dismissAuthDialog()
    }
  }

  private suspend fun fetchAccessTokenAndAccountInfoAsync(code: String): Pair<OAuthTokenResponse, ActorGetProfileResponse>? {

    return withContext(Dispatchers.Default) {

      try {
        val authResponse = AuthFactory
          .instance(BSKY_SOCIAL.uri)
          .oauth()
          .authorizationCodeTokenRequest(
            oauthContext!!,
            OAuthAuthorizationCodeTokenRequest().also {
              it.code = code
            }
          )

        println("authResponse[${authResponse.data}]")


        // 自分の ScreenName 等を取得するために profile を取得する

        val myDid = authResponse.data.sub

        val authProvider = OAuthProvider(
          accessTokenJwt = authResponse.data.accessToken,
          refreshTokenJwt = authResponse.data.refreshToken,
          session = oauthContext!!,
        )

        val profileResponse = BlueskyFactory.instance(authProvider.pdsEndpoint)
          .actor()
          .getProfile(
            ActorGetProfileRequest(
              authProvider
            ).also {
              it.actor = myDid
            }
          )

        val user = profileResponse.data
        println("user handle[${user.handle}]")


        Pair(authResponse.data, user)

      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    }
  }
}

val AuthProvider.pdsEndpoint: String
  get() = didToUrl(this.pdsDid)

/**
 * did を URL に変換する
 *
 * 例えば
 * "did:web:shimeji.us-east.host.bsky.network"
 * を
 * "https://shimeji.us-east.host.bsky.network"
 * に変更する
 */
fun didToUrl(did: String): String {
  return "https://" + did.substringAfterLast(":")
}
