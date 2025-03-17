package jp.takke.tweenb.app.domain

import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.repository.AccountRepository
import jp.takke.tweenb.app.util.LoggerWrapper
import work.socialhub.kbsky.Bluesky
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.auth.AuthFactory
import work.socialhub.kbsky.auth.OAuthContext
import work.socialhub.kbsky.auth.OAuthProvider
import work.socialhub.kbsky.auth.api.entity.oauth.OAuthRefreshTokenRequest
import java.lang.String.format

/**
 * Blueskyクライアントのインターフェース
 */
interface BlueskyClient {
  val account: Account?

  /**
   * クライアントが正しく初期化されているかどうか
   */
  fun isInitialized(): Boolean

  /**
   * クライアントを初期化する
   */
  fun initialize(account: Account)

  /**
   * 認証プロバイダーを取得する
   */
  fun getAuthProvider(): OAuthProvider?

  /**
   * Bluesky APIを実行する（トークンの自動リフレッシュ付き）
   */
  suspend fun <T> executeWithAutoRefresh(request: suspend (Bluesky) -> T): T

  companion object {
    /**
     * クライアントのインスタンスを生成します
     */
    fun create(): BlueskyClient {
      return BlueskyClientImpl()
    }
  }
}

/**
 * Blueskyクライアントの実装クラス
 */
private class BlueskyClientImpl : BlueskyClient {

  private val logger = LoggerWrapper("BlueskyClient")

  // Bluesky APIのファクトリー
  private val factory: Bluesky
    get() = BlueskyFactory.instance("${resolvePds()}/")

  private fun resolvePds(): String {
    val authProvider = authProvider
      ?: throw IllegalStateException("AuthProvider is not initialized.")
    return authProvider.pdsEndpoint
  }

  // 認証プロバイダー
  private var authProvider: OAuthProvider? = null

  override var account: Account? = null
    private set

  override fun isInitialized(): Boolean {
    return account != null && authProvider != null
  }

  override fun initialize(account: Account) {
    this.account = account

    // 認証プロバイダーを初期化
    this.authProvider = OAuthProvider(
      accessTokenJwt = account.accessJwt,
      refreshTokenJwt = account.refreshJwt,
      session = createOAuthContext(account)
    )
  }

  /**
   * 認証プロバイダーを取得する
   */
  override fun getAuthProvider(): OAuthProvider? {
    return authProvider
  }

  /**
   * OAuthContextを作成する
   */
  private fun createOAuthContext(account: Account): OAuthContext {
    return OAuthContext().also {
      it.clientId = AppConstants.OAUTH_CLIENT_ID
      it.dPoPNonce = account.dPoPNonce
      it.publicKey = account.publicKey
      it.privateKey = account.privateKey
    }
  }

  /**
   * Bluesky の API を呼び出す
   *
   * トークンの自動リフレッシュ＆保存も行う
   */
  override suspend fun <T> executeWithAutoRefresh(
    request: suspend (Bluesky) -> T
  ): T {

    //--------------------------------------------------
    // exp が近い場合はリフレッシュする
    //--------------------------------------------------
    val exp = authProvider!!.jwt.exp
    val now = System.currentTimeMillis() / 1000
    val remainSec = exp - now
    if (remainSec < 60) {
      logger.i("exp が近いので refresh する: remain[${remainSec}s=${secToHMS(remainSec)}]")

      // トークンリフレッシュは直列化する
      val repository = AccountRepository.instance
      val updatedAccount = refreshTokenForOAuth(repository)
      initialize(updatedAccount)
    }

    try {
      //--------------------------------------------------
      // リクエスト実行
      //--------------------------------------------------
      return request.invoke(factory).also {

        // dPoPNonce を保存する
        // TODO 実装すること
//        bskyAccountProvider.updateBlueskyDPoPNonce(accountIdWIN, client.oAuthSession?.dPoPNonce)
      }
    } catch (e: Exception) {
      logger.e("API呼び出しエラー: ${e.message}", e)
      throw e
    }
  }

  private fun secToHMS(remainSec0: Long): String {
    val remainSec = if (remainSec0 < 0) -remainSec0 else remainSec0
    val minusFlag = if (remainSec0 < 0) "-" else ""

    val day = remainSec / (60 * 60 * 24)
    val hour = (remainSec % (60 * 60 * 24)) / (60 * 60)
    val min = (remainSec % (60 * 60)) / 60
    val sec = remainSec % 60

    return minusFlag + if (day > 0) {
      format("%dd %02d:%02d:%02d", day, hour, min, sec)
    } else {
      format("%02d:%02d:%02d", hour, min, sec)
    }
  }

  /**
   * OAuth 方式のトークンリフレッシュ
   */
  @Synchronized
  private fun refreshTokenForOAuth(repository: AccountRepository): Account {

    // 同時にリフレッシュした場合、Synchronized で排他制御されているため同時実行はされないが、
    // 直前のリフレッシュで呼び出し元の client はリフレッシュトークンが書き換わっている可能性があるため、ここで client を再取得する
    val account = account ?: throw IllegalStateException("BlueskyClient が初期化されていません")

    val oAuthContext = OAuthContext().also {
      it.clientId = AppConstants.OAUTH_CLIENT_ID
      it.redirectUri = AppConstants.CALLBACK_URL
      it.publicKey = account.publicKey
      it.privateKey = account.privateKey
      it.dPoPNonce = account.dPoPNonce
    }

    val authProvider = authProvider ?: throw IllegalStateException("OAuthProvider が初期化されていません")
    logger.d("OAuth refresh 開始: dPoPNonce[${oAuthContext.dPoPNonce}], refreshToken[${authProvider.refreshTokenJwt}]")
    val response = AuthFactory
      .instance(authProvider.pdsEndpoint)
      .oauth()
      .refreshTokenRequest(
        oAuthContext,
        OAuthRefreshTokenRequest(authProvider)
      )

    val rr = response.data
    logger.d("refresh response: $rr")

    // Refresh したトークンを保存する
    val updatedAccount = Account(
      accountId = account.accountId,
      screenName = account.screenName,
      accessJwt = rr.accessToken,
      refreshJwt = rr.refreshToken,
      dPoPNonce = oAuthContext.dPoPNonce,
      publicKey = oAuthContext.publicKey ?: "",
      privateKey = oAuthContext.privateKey ?: "",
    )
    // アカウント情報を永続化
    repository.saveAccount(account)
    logger.i("refresh 完了: updated[$updatedAccount]")

    return updatedAccount
  }
}