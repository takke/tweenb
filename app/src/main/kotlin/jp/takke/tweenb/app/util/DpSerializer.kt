package jp.takke.tweenb.app.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Dp型をシリアライズするためのカスタムシリアライザ
 */
object DpSerializer : KSerializer<Dp> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Dp", PrimitiveKind.FLOAT)

  override fun serialize(encoder: Encoder, value: Dp) {
    encoder.encodeFloat(value.value)
  }

  override fun deserialize(decoder: Decoder): Dp {
    return decoder.decodeFloat().dp
  }
} 