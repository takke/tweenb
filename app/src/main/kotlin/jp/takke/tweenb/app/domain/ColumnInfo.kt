package jp.takke.tweenb.app.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ColumnInfo(
  val type: ColumnType,
  val name: String,
  @Serializable(with = DpSerializer::class)
  val initialWidth: Dp,
  @Transient
  val width: MutableState<Dp> = mutableStateOf(initialWidth),
  @SerialName("width")
  @Serializable(with = DpSerializer::class)
  private var _serializedWidth: Dp? = null
) {
  // デシリアライズ後に呼び出すメソッド
  fun initializeWidth() {
    // 保存された幅があればそれを使用、なければinitialWidthを使用
    width.value = _serializedWidth ?: initialWidth
  }

  // シリアライズ前に呼び出すメソッド
  fun prepareForSerialization() {
    _serializedWidth = width.value
  }
}