package jp.takke.tweenb.app.repository

import androidx.compose.ui.graphics.toAwtImage
import jp.takke.tweenb.app.AppConstants
import jp.takke.tweenb.app.domain.ImageAttachment
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.com.atproto.repo.RepoUploadBlobRequest
import work.socialhub.kbsky.model.app.bsky.embed.EmbedImages
import work.socialhub.kbsky.model.app.bsky.embed.EmbedImagesImage
import work.socialhub.kbsky.model.app.bsky.richtext.RichtextFacet
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * 投稿機能を提供するリポジトリクラス
 */
class PostRepository private constructor(
  private val blueskyClient: BlueskyClient
) {
  private val logger = LoggerWrapper("PostRepository")

  /**
   * テキスト投稿を行う
   *
   * @param text 投稿するテキスト
   * @param images 添付画像リスト
   * @return 投稿が成功したらtrue
   */
  suspend fun createPost(
    text: String,
    images: List<ImageAttachment> = emptyList()
  ): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        if (!blueskyClient.isInitialized()) {
          logger.e("BlueskyClientが初期化されていません")
          return@withContext false
        }

        // リッチテキスト処理
        val (processedText, facets) = processText(text)

        // 画像アップロード
        val uploadedImages = if (images.isNotEmpty()) {
          logger.i("画像をアップロード開始: ${images.size}枚")
          images.mapNotNull { attachment ->
            uploadImage(attachment)
          }
        } else {
          emptyList()
        }

        // 投稿作成
        val response = blueskyClient.executeWithAutoRefresh { bluesky ->
          val request = FeedPostRequest(blueskyClient.getAuthProvider()!!).also { request ->
            request.text = processedText

            // リンクやメンションがある場合はfacetsを設定
            if (facets.isNotEmpty()) {
              request.facets = facets
            }

            // 画像を添付
            if (uploadedImages.isNotEmpty()) {
              request.embed = EmbedImages().apply {
                this.images = uploadedImages
              }
            }

            request.via = AppConstants.APP_NAME
          }

          bluesky.feed().post(request)
        }

        logger.i("投稿成功: $response")
        true
      } catch (e: Exception) {
        logger.e("投稿エラー: ${e.message}", e)
        false
      }
    }
  }

  /**
   * 画像をアップロードする
   *
   * @param attachment 画像の添付情報
   * @return アップロードされた画像情報、失敗した場合はnull
   */
  private suspend fun uploadImage(attachment: ImageAttachment): EmbedImagesImage? {
    return try {
      // ImageBitmapをBufferedImageに変換
      val awtImage = attachment.image.toAwtImage()
      val bufferedImage = BufferedImage(
        awtImage.width,
        awtImage.height,
        BufferedImage.TYPE_INT_ARGB
      )

      val graphics = bufferedImage.createGraphics()
      graphics.drawImage(awtImage, 0, 0, null)
      graphics.dispose()

      // BufferedImageをバイト配列に変換
      val outputStream = ByteArrayOutputStream()
      ImageIO.write(bufferedImage, "png", outputStream)
      val imageBytes = outputStream.toByteArray()

      // 画像をアップロード
      val uploadedBlob = blueskyClient.executeWithAutoRefresh { bluesky ->
        bluesky.repo().uploadBlob(
          RepoUploadBlobRequest(
            auth = blueskyClient.getAuthProvider()!!,
            bytes = imageBytes,
            name = "image.png",
            contentType = "image/png",
          )
        )
      }

      // アップロードされた画像情報を返す
      val image = EmbedImagesImage()
      image.alt = attachment.alt
      image.image = uploadedBlob.data.blob

      logger.i("画像アップロード成功: ${image.image?.size}, ${image.image?.type}")

      image
    } catch (e: Exception) {
      logger.e("画像アップロードエラー: ${e.message}", e)
      null
    }
  }

  /**
   * テキストを処理して、リンクやメンションなどを抽出する
   *
   * @param text 元のテキスト
   * @return 処理されたテキストとFacets
   */
  private fun processText(text: String): Pair<String, List<RichtextFacet>> {
    // TODO 実装すること
    return Pair(text, emptyList())
  }

  companion object {
    private var instance: PostRepository? = null

    fun getInstance(blueskyClient: BlueskyClient): PostRepository {
      return instance ?: PostRepository(blueskyClient).also {
        instance = it
      }
    }
  }
} 