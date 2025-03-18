package jp.takke.tweenb.app.repository

import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.model.app.bsky.richtext.RichtextFacet

/**
 * 投稿機能を提供するリポジトリクラス
 */
class PostRepository private constructor(
  private val blueskyClient: BlueskyClient
) {
  private val logger = LoggerWrapper("PostRepository")

  /**
   * テキスト投稿を行う
   *
   * @param text 投稿するテキスト
   * @return 投稿が成功したらtrue
   */
  suspend fun createPost(text: String): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        if (!blueskyClient.isInitialized()) {
          logger.e("BlueskyClientが初期化されていません")
          return@withContext false
        }

        // リッチテキスト処理
        val (processedText, facets) = processText(text)

        // 投稿作成
        val response = blueskyClient.executeWithAutoRefresh { bluesky ->
          bluesky.feed().post(
            FeedPostRequest(blueskyClient.getAuthProvider()!!).also { request ->
              request.text = processedText

              // リンクやメンションがある場合はfacetsを設定
              if (facets.isNotEmpty()) {
                request.facets = facets
              }

              request.via = AppConstants.APP_NAME
            }
          )
        }

        logger.i("投稿成功: $response")
        true
      } catch (e: Exception) {
        logger.e("投稿エラー: ${e.message}", e)
        false
      }
    }
  }

  /**
   * テキストを処理して、リンクやメンションなどを抽出する
   *
   * @param text 元のテキスト
   * @return 処理されたテキストとFacets
   */
  private fun processText(text: String): Pair<String, List<RichtextFacet>> {
    // TODO 実装すること
    return Pair(text, emptyList())
  }

  companion object {
    private var instance: PostRepository? = null

    fun getInstance(blueskyClient: BlueskyClient): PostRepository {
      return instance ?: PostRepository(blueskyClient).also {
        instance = it
      }
    }
  }
} 