package jp.takke.tweenb.app.util

/**
 * Loggerクラスのラッパー
 * クラス名などのタグを内部に保持し、ログ出力時に自動的に指定する
 */
class LoggerWrapper(private val tag: String) {

  private val logger = Logger.instance

  /**
   * デバッグログ
   */
  fun d(message: String) {
    logger.d(tag, message)
  }

  /**
   * 情報ログ
   */
  fun i(message: String) {
    logger.i(tag, message)
  }

  /**
   * 警告ログ
   */
  fun w(message: String, throwable: Throwable? = null) {
    logger.w(tag, message, throwable)
  }

  /**
   * エラーログ
   */
  fun e(message: String, throwable: Throwable? = null) {
    logger.e(tag, message, throwable)
  }
}