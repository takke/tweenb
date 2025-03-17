package jp.takke.tweenb.app.repository

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import kotlinx.serialization.json.*
import java.io.File
import java.util.*

/**
 * アプリケーション設定を管理するリポジトリクラス
 */
class AppPropertyRepository {
  // 設定ファイルのパス
  private val prefsFile = File(System.getProperty("user.home"), ".tweenb.properties")

  // プロパティオブジェクト
  private val props = Properties()

  // JSONシリアライザ
  private val json = Json { prettyPrint = true }

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
    val x = props.getProperty("window.x")?.toFloatOrNull() ?: 300f
    val y = props.getProperty("window.y")?.toFloatOrNull() ?: 300f
    return WindowPosition(x.dp, y.dp)
  }

  /**
   * ウィンドウサイズを取得
   */
  fun getWindowSize(): DpSize {
    val width = props.getProperty("window.width")?.toFloatOrNull() ?: 800f
    val height = props.getProperty("window.height")?.toFloatOrNull() ?: 600f
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
   * プロパティファイルを保存
   */
  private fun saveProperties() {
    prefsFile.outputStream().use {
      props.store(it, "tweenb window settings")
    }
  }

  /**
   * アカウント情報を保存
   */
  fun saveAccount(
    accountId: String,
    screenName: String,
    accessJwt: String,
    refreshJwt: String,
    dPoPNonce: String,
    publicKey: String,
    privateKey: String,
  ) {
    // アカウント情報をJSONオブジェクトとして構築
    val accountJson = buildJsonObject {
      put("accountId", JsonPrimitive(accountId))
      put("screenName", JsonPrimitive(screenName))
      put("accessJwt", JsonPrimitive(accessJwt))
      put("refreshJwt", JsonPrimitive(refreshJwt))
      put("dPoPNonce", JsonPrimitive(dPoPNonce))
      put("publicKey", JsonPrimitive(publicKey))
      put("privateKey", JsonPrimitive(privateKey))
    }

    // 既存のアカウントリストを取得
    val accountsStr = props.getProperty("accounts", "[]")
    val accounts = try {
      json.parseToJsonElement(accountsStr).jsonArray
    } catch (e: Exception) {
      // パースエラーの場合は空の配列を使用
      JsonArray(emptyList())
    }

    // 同じアカウントIDが存在する場合は更新、なければ追加
    val updatedAccounts = buildJsonArray {
      var found = false
      for (account in accounts) {
        if (account is JsonObject && account["accountId"]?.jsonPrimitive?.content == accountId) {
          add(accountJson)
          found = true
        } else {
          add(account)
        }
      }
      if (!found) {
        add(accountJson)
      }
    }

    // プロパティに保存
    props.setProperty("accounts", updatedAccounts.toString())
    saveProperties()
  }
} 