package jp.takke.tweenb.app.domain

import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.domain.Service

/**
 * Blueskyクライアントのインターフェース
 * 通信や認証処理は不要な段階なので、ダミー実装を返すようにします
 */
interface BlueskyClient {
  /**
   * クライアントが正しく初期化されているかどうか
   */
  fun isInitialized(): Boolean

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

  override fun isInitialized(): Boolean {
    // TODO 実装すること
    return false
  }
} 