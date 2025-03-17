package jp.takke.tweenb.app.domain

import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.domain.Service

/**
 * Blueskyクライアントのインターフェース
 * 通信や認証処理は不要な段階なので、ダミー実装を返すようにします
 */
interface BlueskyClient {
  val account: Account?

  /**
   * クライアントが正しく初期化されているかどうか
   */
  fun isInitialized(): Boolean

  fun initialize(account: Account)

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
  private val factory = BlueskyFactory.instance(Service.BSKY_SOCIAL.uri)

  override var account: Account? = null
    private set

  override fun isInitialized(): Boolean {
    return account != null
  }

  override fun initialize(account: Account) {
    this.account = account
  }
} 