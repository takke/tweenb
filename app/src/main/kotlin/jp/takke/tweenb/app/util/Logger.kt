package jp.takke.tweenb.app.util

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * アプリケーションログを管理するクラス
 * コンソール出力とファイル出力の両方を行う
 */
class Logger private constructor() {

  // ログレベル
  enum class Level {
    DEBUG, INFO, WARN, ERROR
  }

  // ログファイル名のフォーマット
  private val logFileNameDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

  // ログエントリのタイムスタンプフォーマット
  private val logTimestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

  // ログファイルのディレクトリ
  private val logDir: File = File(System.getProperty("user.home"), ".tweenb/logs").apply {
    if (!exists()) {
      mkdirs()
    }
  }

  // ログキュー（非同期処理用）
  private val logQueue = ConcurrentLinkedQueue<LogEntry>()

  // 処理中フラグ
  private val processing = AtomicBoolean(false)

  // ロガースレッド
  private val loggerThread = Thread {
    while (true) {
      processLogQueue()
      try {
        Thread.sleep(100)
      } catch (e: InterruptedException) {
        // 割り込まれた場合は終了
        break
      }
    }
  }.apply {
    isDaemon = true
    name = "Logger"
    start()
  }

  // ログエントリ
  private data class LogEntry(
    val level: Level,
    val tag: String,
    val message: String,
    val timestamp: Date = Date(),
    val throwable: Throwable? = null
  )

  /**
   * ログキューの処理
   */
  private fun processLogQueue() {
    if (logQueue.isEmpty() || !processing.compareAndSet(false, true)) {
      return
    }

    try {
      val currentDate = Date()
      val logFileName = "tweenb_${logFileNameDateFormat.format(currentDate)}.log"
      val logFile = File(logDir, logFileName)

      // ファイルに追記モードで開く
      FileWriter(logFile, true).use { fileWriter ->
        PrintWriter(fileWriter).use { printWriter ->
          var entry = logQueue.poll()
          while (entry != null) {
            // ログエントリをフォーマット
            val logLine = formatLogEntry(entry)

            // コンソール出力
            println(logLine)

            // ファイル出力
            printWriter.println(logLine)

            // スタックトレースがあれば出力
            entry.throwable?.let { throwable ->
              throwable.printStackTrace(System.out)
              throwable.printStackTrace(printWriter)
            }

            entry = logQueue.poll()
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      processing.set(false)
    }
  }

  /**
   * ログエントリをフォーマット
   */
  private fun formatLogEntry(entry: LogEntry): String {
    val timestamp = logTimestampFormat.format(entry.timestamp)
    return "[$timestamp] [${entry.level}] [${entry.tag}] ${entry.message}"
  }

  /**
   * デバッグログ
   */
  fun d(tag: String, message: String) {
    log(Level.DEBUG, tag, message)
  }

  /**
   * 情報ログ
   */
  fun i(tag: String, message: String) {
    log(Level.INFO, tag, message)
  }

  /**
   * 警告ログ
   */
  fun w(tag: String, message: String, throwable: Throwable? = null) {
    log(Level.WARN, tag, message, throwable)
  }

  /**
   * エラーログ
   */
  fun e(tag: String, message: String, throwable: Throwable? = null) {
    log(Level.ERROR, tag, message, throwable)
  }

  /**
   * ログ出力の共通処理
   */
  private fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
    logQueue.add(LogEntry(level, tag, message, Date(), throwable))
  }

  companion object {
    // シングルトンインスタンス
    val instance: Logger by lazy { Logger() }
  }
} 