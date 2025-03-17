package jp.takke.tweenb.app.repository

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import jp.takke.tweenb.app.domain.Account
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

/**
 * アプリケーション設定を管理するリポジトリクラス
 */
class AppPropertyRepository private constructor() {
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
      props.store(it, "tweenb settings")
    }
  }

  /**
   * アカウント情報を保存
   */
  fun saveAccount(account: Account) {

    // 既存のアカウントリストを取得
    val accounts = getAccounts().toMutableList()

    // 同じアカウントIDが存在する場合は更新、なければ追加
    val index = accounts.indexOfFirst { it.accountId == account.accountId }
    if (index >= 0) {
      accounts[index] = account
    } else {
      accounts.add(account)
    }

    // アカウントリストをJSON配列に変換して保存
    val accountsJson = json.encodeToString(accounts)
    props.setProperty("accounts", accountsJson)
    saveProperties()
  }

  /**
   * 全アカウント情報を取得
   */
  fun getAccounts(): List<Account> {
    val accountsStr = props.getProperty("accounts", "[]")
    return try {
      json.decodeFromString<List<Account>>(accountsStr)
    } catch (e: Exception) {
      // パースエラーの場合は空のリストを返す
      emptyList()
    }
  }

  /**
   * アカウントIDを指定してアカウント情報を取得
   * @return 見つからない場合はnull
   */
  fun getAccount(accountId: String): Account? {
    return getAccounts().find { it.accountId == accountId }
  }

  /**
   * アカウント情報を削除
   */
  fun deleteAccount(accountId: String): Boolean {
    val accounts = getAccounts().toMutableList()
    val initialSize = accounts.size
    accounts.removeIf { it.accountId == accountId }

    if (accounts.size != initialSize) {
      // アカウントリストをJSON配列に変換して保存
      val accountsJson = json.encodeToString(accounts)
      props.setProperty("accounts", accountsJson)
      saveProperties()
      return true
    }
    return false
  }

  companion object {
    val instance by lazy { AppPropertyRepository() }
  }
} 