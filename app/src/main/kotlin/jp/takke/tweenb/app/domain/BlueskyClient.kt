package jp.takke.tweenb.app.domain

import jp.takke.tweenb.app.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.Bluesky
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedGetTimelineRequest
import work.socialhub.kbsky.auth.OAuthProvider
import java.text.SimpleDateFormat
import java.util.*

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
  private fun createOAuthContext(account: Account): work.socialhub.kbsky.auth.OAuthContext {
    return work.socialhub.kbsky.auth.OAuthContext().also {
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

        val response = factory.feed().getTimeline(request)

        // レスポンスから投稿のリストを取得し、BsFeedViewPostに変換
        response.data.feed
//        response.data.feed.map { feedView ->
//          val post = feedView.post
//          val author = post.author
//
//          BsFeedViewPost(
//            author = BsFeedViewPost.Author(
//              handle = author.handle,
//              displayName = author.displayName,
//              avatar = author.avatar,
//              did = author.did
//            ),
//            record = BsFeedViewPost.Record(
//              text = post.record.text,
//              createdAt = post.record.createdAt
//            ),
//            indexedAt = parseDate(post.indexedAt),
//            uri = post.uri,
//            cid = post.cid
//          )
//        }
      } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
      }
    }
  }
  
  /**
   * 日付文字列をDateオブジェクトに変換する
   */
  private fun parseDate(dateStr: String): Date {
    return try {
      // ISO 8601形式の日付文字列をパース
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
      format.timeZone = TimeZone.getTimeZone("UTC")
      format.parse(dateStr) ?: Date()
    } catch (e: Exception) {
      e.printStackTrace()
      Date()
    }
  }
} 