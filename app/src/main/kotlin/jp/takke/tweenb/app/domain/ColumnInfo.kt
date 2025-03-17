package jp.takke.tweenb.app.domain

import androidx.compose.ui.unit.Dp

data class ColumnInfo(
  val type: ColumnType,
  val name: String,
  val width: Dp,
)