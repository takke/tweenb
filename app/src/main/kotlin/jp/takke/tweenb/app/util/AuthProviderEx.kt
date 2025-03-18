package jp.takke.tweenb.app.util

import work.socialhub.kbsky.auth.AuthProvider

val AuthProvider.pdsEndpoint: String
  get() = didToUrl(this.pdsDid)

/**
 * did を URL に変換する
 *
 * 例えば
 * "did:web:shimeji.us-east.host.bsky.network"
 * を
 * "https://shimeji.us-east.host.bsky.network"
 * に変更する
 */
private fun didToUrl(did: String): String {
  return "https://" + did.substringAfterLast(":")
}