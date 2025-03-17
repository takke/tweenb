package jp.takke.tweenb.app.domain

import androidx.compose.ui.unit.Dp
import kotlinx.serialization.Serializable

@Serializable
data class ColumnInfo(
  val type: ColumnType,
  val name: String,
  @Serializable(with = DpSerializer::class)
  val width: Dp,
)