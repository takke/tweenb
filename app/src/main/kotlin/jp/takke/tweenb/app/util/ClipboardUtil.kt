package jp.takke.tweenb.app.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.jetbrains.skia.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * クリップボード操作に関するユーティリティクラス
 */
object ClipboardUtil {

  /**
   * クリップボードから画像を取得する
   *
   * @return 画像が存在する場合はImageBitmapを返す、存在しない場合はnullを返す
   */
  fun getImageFromClipboard(): ImageBitmap? {
    try {
      val clipboard = Toolkit.getDefaultToolkit().systemClipboard

      // クリップボードが画像を含むか確認
      if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
        // 画像を取得
        val image = clipboard.getData(DataFlavor.imageFlavor) as java.awt.Image

        // BufferedImageに変換
        val bufferedImage = convertToBufferedImage(image)

        // ByteArrayに変換
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        // ComposeのImageBitmapに変換
        val skiaImage = Image.makeFromEncoded(imageBytes)
        return org.jetbrains.skia.Bitmap.makeFromImage(skiaImage).asImageBitmap()
      }
    } catch (e: Exception) {
      // エラーハンドリング
      e.printStackTrace()
    }

    return null
  }

  /**
   * java.awt.Imageをjava.awt.image.BufferedImageに変換する
   */
  private fun convertToBufferedImage(image: java.awt.Image): BufferedImage {
    // 既にBufferedImageの場合はそのまま返す
    if (image is BufferedImage) {
      return image
    }

    // 画像サイズを取得
    val width = image.getWidth(null)
    val height = image.getHeight(null)

    // 新しいBufferedImageを作成
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    // 画像を描画
    val graphics = bufferedImage.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()

    return bufferedImage
  }
} 