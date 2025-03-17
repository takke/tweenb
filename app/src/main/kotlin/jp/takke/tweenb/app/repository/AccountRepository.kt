package jp.takke.tweenb.app.repository

import jp.takke.tweenb.app.domain.Account
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * アカウント情報を管理するリポジトリクラス
 */
class AccountRepository private constructor(
  private val appPropertyRepository: AppPropertyRepository
) {
  // JSONシリアライザ
  private val json = Json { prettyPrint = true }

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
    appPropertyRepository.setProperty("accounts", accountsJson)
  }

  /**
   * 全アカウント情報を取得
   */
  fun getAccounts(): List<Account> {
    val accountsStr = appPropertyRepository.getProperty("accounts", "[]")
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
      appPropertyRepository.setProperty("accounts", accountsJson)
      return true
    }
    return false
  }

  companion object {
    val instance by lazy {
      AccountRepository(AppPropertyRepository.instance)
    }
  }
} 