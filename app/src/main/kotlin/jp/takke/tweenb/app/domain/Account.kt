package jp.takke.tweenb.app.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Blueskyアカウント情報
 */
@Serializable
data class Account(
  // アカウントID (did:plc:xxx 形式)
  @SerialName("accountId")
  val accountId: String,

  // スクリーンネーム (handle)
  @SerialName("screenName")
  val screenName: String,

  // アクセストークン
  @SerialName("accessJwt")
  val accessJwt: String,

  // リフレッシュトークン
  @SerialName("refreshJwt")
  val refreshJwt: String,

  // DPoP Nonce
  @SerialName("dPoPNonce")
  val dPoPNonce: String,

  // 公開鍵
  @SerialName("publicKey")
  val publicKey: String,

  // 秘密鍵
  @SerialName("privateKey")
  val privateKey: String
) {
  override fun toString(): String {
    return "Account(accountId='$accountId', screenName='$screenName', accessJwt='$accessJwt', refreshJwt='$refreshJwt', dPoPNonce='$dPoPNonce', publicKey='***', privateKey='***')"
  }
}