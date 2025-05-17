package jp.takke.tweenb.app.repository

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

/**
 * アプリケーション設定を管理するリポジトリクラス
 */
class AppPropertyRepository private constructor() {
  private val logger = LoggerWrapper("AppPropertyRepository")

  // 設定ファイルのパス
  private val prefsFile = File(System.getProperty("user.home"), ".tweenb.properties")

  // プロパティオブジェクト
  private val props = Properties()

  // JSONシリアライザ
  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  init {
    // 保存されている設定を読み込む
    loadProperties()
  }

  /**
   * プロパティファイルを読み込む
   */
  private fun loadProperties() {
    if (prefsFile.exists()) {
      prefsFile.inputStream().use {
        props.load(it)
      }
    }
  }

  /**
   * ウィンドウ位置を取得
   */
  fun getWindowPosition(): WindowPosition {
    val x = props.getProperty("window.x")?.toFloatOrNull() ?: 100f
    val y = props.getProperty("window.y")?.toFloatOrNull() ?: 100f
    return WindowPosition(x.dp, y.dp)
  }

  /**
   * ウィンドウサイズを取得
   */
  fun getWindowSize(): DpSize {
    val width = props.getProperty("window.width")?.toFloatOrNull() ?: 1000f
    val height = props.getProperty("window.height")?.toFloatOrNull() ?: 800f
    return DpSize(width.dp, height.dp)
  }

  /**
   * ウィンドウ位置を保存
   */
  fun saveWindowPosition(position: WindowPosition) {
    props.setProperty("window.x", position.x.value.toString())
    props.setProperty("window.y", position.y.value.toString())
    saveProperties()
  }

  /**
   * ウィンドウサイズを保存
   */
  fun saveWindowSize(size: DpSize) {
    props.setProperty("window.width", size.width.value.toString())
    props.setProperty("window.height", size.height.value.toString())
    saveProperties()
  }

  /**
   * ウィンドウ設定を一括保存
   */
  fun saveWindowState(position: WindowPosition, size: DpSize) {
    props.setProperty("window.x", position.x.value.toString())
    props.setProperty("window.y", position.y.value.toString())
    props.setProperty("window.width", size.width.value.toString())
    props.setProperty("window.height", size.height.value.toString())
    saveProperties()
  }

  /**
   * カラム情報のリストを保存する
   * @param columns 保存するカラム情報のリスト
   */
  fun saveColumns(columns: List<ColumnInfo>) {
    try {
      // シリアライズ前に各カラムの現在の幅を保存用フィールドにセット
      columns.forEach { it.prepareForSerialization() }

      val columnsJson = json.encodeToString(columns)
      props.setProperty("columns.layout", columnsJson)
      saveProperties()
    } catch (e: Exception) {
      logger.e("カラム情報の保存に失敗しました: ${e.message}", e)
    }
  }

  /**
   * カラム情報のリストを取得する
   * @return 保存されているカラム情報のリスト、または空のリスト
   */
  fun getColumns(): List<ColumnInfo>? {
    val columnsJson = props.getProperty("columns.layout") ?: return null
    return try {
      val columns = json.decodeFromString<List<ColumnInfo>>(columnsJson)
      // デシリアライズ後に幅を初期化
      columns.forEach { it.initializeWidth() }
      columns
    } catch (e: Exception) {
      logger.e("カラム情報の読み込みに失敗しました: ${e.message}", e)
      null
    }
  }

  /**
   * プロパティファイルを保存
   */
  private fun saveProperties() {
    prefsFile.outputStream().use { stream ->
      props.store(stream, "tweenb settings")
      stream.flush()
    }
  }

  /**
   * 指定されたキーの値を取得する
   */
  fun getProperty(key: String, defaultValue: String = ""): String {
    return props.getProperty(key, defaultValue)
  }

  /**
   * 指定されたキーに値を設定する
   */
  fun setProperty(key: String, value: String) {
    props.setProperty(key, value)
    saveProperties()
  }

  /**
   * 自動更新が有効かどうかを取得する
   */
  fun isAutoRefreshEnabled(): Boolean {
    return props.getProperty("timeline.autoRefresh.enabled", "false").toBoolean()
  }

  /**
   * 自動更新の有効/無効を設定する
   */
  fun setAutoRefreshEnabled(enabled: Boolean) {
    props.setProperty("timeline.autoRefresh.enabled", enabled.toString())
    saveProperties()
  }

  /**
   * 自動更新間隔（秒）を取得する
   */
  fun getAutoRefreshInterval(): Int {
    return props.getProperty(
      "timeline.autoRefresh.interval",
      AppConstants.DEFAULT_AUTO_REFRESH_INTERVAL.toString()
    ).toInt()
  }

  /**
   * 自動更新間隔（秒）を設定する
   */
  fun setAutoRefreshInterval(intervalSeconds: Int) {
    props.setProperty("timeline.autoRefresh.interval", intervalSeconds.toString())
    saveProperties()
  }

  /**
   * タイムラインの表示行数を取得する
   */
  fun getTimelineVisibleLines(): Int {
    return props.getProperty("timeline.visibleLines", AppConstants.DEFAULT_TIMELINE_VISIBLE_LINES.toString()).toInt()
  }

  /**
   * タイムラインの表示行数を設定する
   */
  fun setTimelineVisibleLines(lines: Int) {
    props.setProperty("timeline.visibleLines", lines.toString())
    saveProperties()
  }

  /**
   * ツールチップ表示が有効かどうかを取得する
   */
  fun isTooltipEnabled(): Boolean {
    return props.getProperty("timeline.tooltip.enabled", AppConstants.DEFAULT_TOOLTIP_ENABLED.toString()).toBoolean()
  }

  /**
   * ツールチップ表示の有効/無効を設定する
   */
  fun setTooltipEnabled(enabled: Boolean) {
    props.setProperty("timeline.tooltip.enabled", enabled.toString())
    saveProperties()
  }

  companion object {
    val instance by lazy {
      AppPropertyRepository()
    }
  }
} 