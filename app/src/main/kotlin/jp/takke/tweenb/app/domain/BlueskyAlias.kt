package jp.takke.tweenb.app.domain

import jp.takke.tweenb.app.util.BsDateParser
import work.socialhub.kbsky.model.app.bsky.actor.ActorDefsProfileViewBasic
import work.socialhub.kbsky.model.app.bsky.feed.FeedDefsReasonPin
import work.socialhub.kbsky.model.app.bsky.feed.FeedDefsReasonRepost
import work.socialhub.kbsky.model.app.bsky.feed.FeedPost
import java.util.*

typealias BsUserBasic = ActorDefsProfileViewBasic

typealias BsFeedViewPost = work.socialhub.kbsky.model.app.bsky.feed.FeedDefsFeedViewPost

// リポスト判定
val BsFeedViewPost.isRepost: Boolean
  get() = this.reason is FeedDefsReasonRepost

// 固定判定
val BsFeedViewPost.isPinned: Boolean
  get() = this.reason is FeedDefsReasonPin

val BsFeedViewPost.key: String
  get() = if (this.isRepost) {
    this.post.cid + "_" + this.reason?.asReasonRepost?.by?.did
  } else if (this.isPinned) {
    this.post.cid + "_pinned"
  } else {
    this.post.cid ?: ""
  }


typealias BsPost = work.socialhub.kbsky.model.app.bsky.feed.FeedDefsPostView

val BsPost.createdAtAsDate: Date?
  get() = BsDateParser.parseDate((this.record as? FeedPost)?.createdAt)

val BsPost.url: String?
  get() {
    val postId = this.uri?.substringAfterLast("/") ?: return null
    val handle = this.author?.handle ?: return null
    return "https://bsky.app/profile/$handle/post/${postId}"
  }

val BsUserBasic.url: String
  get() = "https://bsky.app/profile/${this.handle}"