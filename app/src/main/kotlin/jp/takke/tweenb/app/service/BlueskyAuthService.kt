package jp.takke.tweenb.app.service

import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.domain.Account
import jp.takke.tweenb.app.repository.AccountRepository
import jp.takke.tweenb.app.util.LoggerWrapper
import jp.takke.tweenb.app.util.pdsEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.ATProtocolException
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.actor.ActorGetProfileRequest
import work.socialhub.kbsky.api.entity.app.bsky.actor.ActorGetProfileResponse
import work.socialhub.kbsky.auth.AuthFactory
import work.socialhub.kbsky.auth.OAuthContext
import work.socialhub.kbsky.auth.OAuthProvider
import work.socialhub.kbsky.auth.api.entity.oauth.BuildAuthorizationUrlRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthAuthorizationCodeTokenRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthPushedAuthorizationRequest
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthTokenResponse
import work.socialhub.kbsky.domain.Service
import java.awt.Desktop
import java.net.URI

/**
 * Bluesky認証サービス
 */
class BlueskyAuthService(
  private val accountRepository: AccountRepository
) {
  // OAuthコンテキスト
  private var oauthContext: OAuthContext? = null

  private val logger = LoggerWrapper("BlueskyAuthService")

  /**
   * OAuth認証プロセスを開始する
   * @param loginHint ログインヒント（ユーザー名）
   * @return 認証URL
   */
  suspend fun startOAuthProcess(loginHint: String = ""): String {
    return withContext(Dispatchers.IO) {
      // OAuth ログイン開始
      logger.d("OAuth ログイン開始")

      oauthContext = OAuthContext().also {
        it.clientId = AppConstants.OAUTH_CLIENT_ID
        it.redirectUri = AppConstants.CALLBACK_URL
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

      val authorizeUrl = AuthFactory
        .instance(Service.BSKY_SOCIAL.uri)
        .oauth()
        .buildAuthorizationUrl(
          oauthContext!!,
          BuildAuthorizationUrlRequest().also {
            it.requestUri = response.data.requestUri
          }
        )

      // ブラウザを開く
      logger.d("OAuth 認証ページを開く")
      Desktop.getDesktop().browse(URI(authorizeUrl))

      authorizeUrl
    }
  }

  /**
   * 認証コードを使用してトークンを取得する
   * @param code 認証コード
   * @return 成功した場合はアカウント情報、失敗した場合はnull
   */
  suspend fun fetchTokenWithCode(code: String): Account? {
    val pair = fetchAccessTokenAndAccountInfoAsync(code) ?: return null

    val (response, user) = pair

    // アカウント情報を作成
    val account = Account(
      accountId = user.did,
      screenName = user.handle,
      accessJwt = response.accessToken,
      refreshJwt = response.refreshToken,
      dPoPNonce = oauthContext?.dPoPNonce ?: "",
      publicKey = oauthContext?.publicKey ?: "",
      privateKey = oauthContext?.privateKey ?: "",
    )

    // アカウント情報を永続化
    accountRepository.saveAccount(account)

    return account
  }

  /**
   * アクセストークンとアカウント情報を取得する
   */
  private suspend fun fetchAccessTokenAndAccountInfoAsync(code: String): Pair<OAuthTokenResponse, ActorGetProfileResponse>? {
    return withContext(Dispatchers.Default) {
      try {
        val authResponse = AuthFactory
          .instance(Service.BSKY_SOCIAL.uri)
          .oauth()
          .authorizationCodeTokenRequest(
            oauthContext!!,
            OAuthAuthorizationCodeTokenRequest().also {
              it.code = code
            }
          )

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

        Pair(authResponse.data, profileResponse.data)
      } catch (e: Exception) {
        logger.e("アクセストークン取得エラー: ${e.message}", e)
        null
      }
    }
  }

  /**
   * エラーメッセージをフォーマットする
   */
  fun formatErrorMessage(e: Exception): String {
    return if (e is ATProtocolException) {
      "$e\n${e.body}\n${e.response}\n${e.cause}"
    } else {
      e.toString()
    }
  }
}
