package jp.takke.tweenb.app.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ColumnType {
  Icon,
  Name,
  ScreenName,
  Post,
  DateTime,
}