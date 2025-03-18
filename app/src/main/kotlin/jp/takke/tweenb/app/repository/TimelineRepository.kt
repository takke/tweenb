package jp.takke.tweenb.app.repository

import jp.takke.tweenb.app.util.BsFeedViewPost
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedGetTimelineRequest

/**
 * タイムライン情報を管理するリポジトリクラス
 */
class TimelineRepository private constructor(
  private val blueskyClient: BlueskyClient
) {
  private val logger = LoggerWrapper("TimelineRepository")

  /**
   * タイムラインを取得する
   *
   * @param limit 取得する投稿の最大数
   * @return 投稿のリスト
   */
  suspend fun getTimeline(limit: Int = 20): List<BsFeedViewPost> {
    return withContext(Dispatchers.IO) {
      try {
        if (!blueskyClient.isInitialized()) {
          return@withContext emptyList()
        }

        val response = blueskyClient.executeWithAutoRefresh { bluesky ->
          bluesky.feed().getTimeline(
            FeedGetTimelineRequest(blueskyClient.getAuthProvider()!!).also {
              it.limit = limit
            }
          )
        }

        response.data.feed
      } catch (e: Exception) {
        logger.e("タイムライン取得エラー: ${e.message}", e)
        throw e
      }
    }
  }

  companion object {
    private var instance: TimelineRepository? = null

    fun getInstance(blueskyClient: BlueskyClient): TimelineRepository {
      return instance ?: TimelineRepository(blueskyClient).also {
        instance = it
      }
    }
  }
} 