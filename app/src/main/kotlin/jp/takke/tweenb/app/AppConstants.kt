package jp.takke.tweenb.app

// アプリケーション全体で使用する定数
object AppConstants {

  const val OAUTH_CLIENT_ID: String = "https://twitpane.com/oauth/bluesky/tweenb/client-metadata.json"
  const val CALLBACK_URL: String = "https://twitpane.com/oauth/bluesky/tweenb/callback"

  // 自動更新間隔（秒）
  val AUTO_REFRESH_INTERVALS = listOf(60, 90, 120, 300, 600)
  const val DEFAULT_AUTO_REFRESH_INTERVAL = 120
}