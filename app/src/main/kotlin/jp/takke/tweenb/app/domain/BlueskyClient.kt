package jp.takke.tweenb.app.domain

import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.repository.AppPropertyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.Bluesky
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedGetTimelineRequest
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

  fun initialize(account: Account)

  /**
   * タイムラインを取得する
   *
   * @param limit 取得する投稿の最大数
   * @return 投稿のリスト
   */
  suspend fun getTimeline(limit: Int = 20): List<BsFeedViewPost>

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
   * タイムラインを取得する
   */
  override suspend fun getTimeline(limit: Int): List<BsFeedViewPost> {
    return withContext(Dispatchers.IO) {
      try {
        if (!isInitialized()) {
          return@withContext emptyList()
        }

        val request = FeedGetTimelineRequest(authProvider!!)
        request.limit = limit

        val response = executeWithAutoRefresh { bluesky ->
          bluesky.feed().getTimeline(request)
        }

        // レスポンスから投稿のリストを取得し、BsFeedViewPostに変換
        response.data.feed
      } catch (e: Exception) {
        e.printStackTrace()
        throw e
      }
    }
  }

  /**
   * Bluesky の API を呼び出す
   *
   * トークンの自動リフレッシュ＆保存も行う
   */
  suspend fun <T> executeWithAutoRefresh(
    request: suspend (Bluesky) -> T
  ): T {

    //--------------------------------------------------
    // exp が近い場合はリフレッシュする
    //--------------------------------------------------
    val exp = authProvider!!.jwt.exp
    val now = System.currentTimeMillis() / 1000
    val remainSec = exp - now
    if (remainSec < 60) {
      println("exp が近いので refresh する: remain[${remainSec}s=${secToHMS(remainSec)}]")

      // トークンリフレッシュは直列化する
      val appPropertyRepository = AppPropertyRepository.instance
      val updatedAccount = refreshTokenForOAuth(appPropertyRepository)
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

      println("API呼び出しエラー: ${e.message}")
      e.printStackTrace()
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
  private fun refreshTokenForOAuth(appPropertyRepository: AppPropertyRepository): Account {

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
    println("OAuth refresh 開始: dPoPNonce[${oAuthContext.dPoPNonce}], refreshToken[${authProvider.refreshTokenJwt}]")
    val response = AuthFactory
      .instance(authProvider.pdsEndpoint)
      .oauth()
      .refreshTokenRequest(
        oAuthContext,
        OAuthRefreshTokenRequest(authProvider)
      )

    val rr = response.data
    println("refresh response: $rr")

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
    appPropertyRepository.saveAccount(account)
    println("refresh 完了: updated[$updatedAccount]")

    return updatedAccount
  }
}