package jp.takke.tweenb.app.domain

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 投稿に添付する画像情報
 */
data class ImageAttachment(
  val image: ImageBitmap,
  val alt: String = ""
) 