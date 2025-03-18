package jp.takke.tweenb.app.domain

import jp.takke.tweenb.app.util.BsDateParser
import work.socialhub.kbsky.model.app.bsky.feed.FeedPost
import java.util.*

typealias BsFeedViewPost = work.socialhub.kbsky.model.app.bsky.feed.FeedDefsFeedViewPost

typealias BsPost = work.socialhub.kbsky.model.app.bsky.feed.FeedDefsPostView

val BsPost.createdAtAsDate: Date?
  get() = BsDateParser.parseDate((this.record as? FeedPost)?.createdAt)

val BsPost.url: String?
  get() {
    val postId = this.uri?.substringAfterLast("/") ?: return null
    val handle = this.author?.handle ?: return null
    return "https://bsky.app/profile/$handle/post/${postId}"
  }
