package jp.takke.tweenb.app.util

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
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

  // スケジューラー（古いログファイル削除用）
  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  init {
    // 起動時に古いログファイルを削除
    cleanupOldLogFiles()

    // 毎日0時に古いログファイルをクリーンアップするスケジュール設定
    scheduleLogCleanup()

    // 初期化ログはここで直接出力
    println("[初期化] ロガーを初期化しました")
  }

  /**
   * 毎日0時に古いログファイルをクリーンアップするスケジュールを設定
   */
  private fun scheduleLogCleanup() {
    val calendar = Calendar.getInstance()
    // 次の日の0時を設定
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    // 次の日の0時までの時間（ミリ秒）を計算
    val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

    // 毎日0時に実行するスケジュールを設定
    scheduler.scheduleAtFixedRate(
      { cleanupOldLogFiles() },
      initialDelay,
      TimeUnit.DAYS.toMillis(1),
      TimeUnit.MILLISECONDS
    )
  }

  /**
   * 前々日以前のログファイルを削除する
   */
  private fun cleanupOldLogFiles() {
    try {
      // 現在の日付を取得
      val calendar = Calendar.getInstance()
      val today = calendar.time

      // 前日の日付を取得
      calendar.add(Calendar.DAY_OF_MONTH, -1)
      val yesterday = calendar.time

      // 保持対象の日付のファイル名を作成
      val todayFileName = "tweenb_${logFileNameDateFormat.format(today)}.log"
      val yesterdayFileName = "tweenb_${logFileNameDateFormat.format(yesterday)}.log"

      // ログディレクトリ内のすべてのファイルを取得
      val logFiles = logDir.listFiles { file ->
        file.isFile && file.name.startsWith("tweenb_") && file.name.endsWith(".log")
      }

      // 削除対象ファイル数をカウント
      var deleteCount = 0

      // 本日と前日以外のログファイルを削除
      logFiles?.forEach { file ->
        if (file.name != todayFileName && file.name != yesterdayFileName) {
          if (file.delete()) {
            deleteCount++
            // 無限ループを避けるためにlog()ではなくprintlnを使用
            println("[${logTimestampFormat.format(Date())}] [INFO] [Logger] 古いログファイルを削除しました: ${file.name}")
          } else {
            println("[${logTimestampFormat.format(Date())}] [WARN] [Logger] ログファイルの削除に失敗しました: ${file.name}")
          }
        }
      }

      // 削除完了のログ
      if (deleteCount > 0) {
        println("[${logTimestampFormat.format(Date())}] [INFO] [Logger] 古いログファイルのクリーンアップ完了: $deleteCount 件削除")
      }
    } catch (e: Exception) {
      // エラーログ
      println("[${logTimestampFormat.format(Date())}] [ERROR] [Logger] ログクリーンアップ中にエラーが発生しました: ${e.message}")
      e.printStackTrace()
    }
  }

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
      println("[${logTimestampFormat.format(Date())}] [ERROR] [Logger] ログ処理中にエラーが発生しました: ${e.message}")
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