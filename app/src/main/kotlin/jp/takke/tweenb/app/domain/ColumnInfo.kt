package jp.takke.tweenb.app.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp

data class ColumnInfo(
  val type: ColumnType,
  val name: String,
  val initialWidth: Dp,
  val width: MutableState<Dp> = mutableStateOf(initialWidth)
)