package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.takke.tweenb.app.AppConstants

@Composable
fun ConfigDialog(
  showConfigDialog: Boolean,
  onDismiss: () -> Unit,
  onShowAuthDialog: () -> Unit,
  onDeleteAccount: () -> Unit,
  accountScreenName: String?,
  // 自動更新設定
  autoRefreshEnabled: Boolean,
  autoRefreshInterval: Int,
  onAutoRefreshToggle: (Boolean) -> Unit,
  onAutoRefreshIntervalChange: (Int) -> Unit,
  // タイムライン表示行数設定
  timelineVisibleLines: Int,
  onTimelineVisibleLinesChange: (Int) -> Unit,
  // ツールチップ表示設定
  tooltipEnabled: Boolean,
  onTooltipEnabledChange: (Boolean) -> Unit,
) {
  if (!showConfigDialog) {
    return
  }

  val showConfirmDialog = remember { mutableStateOf(false) }
  // ドロップダウン表示制御
  val expanded = remember { mutableStateOf(false) }
  val linesExpanded = remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colors.background)
        .padding(16.dp)
        .width(400.dp)
        .height(460.dp)
    ) {
      val authorized = !accountScreenName.isNullOrEmpty()

      // アカウント情報表示
      if (authorized) {
        Text(
          text = "現在のアカウント: @$accountScreenName",
          style = MaterialTheme.typography.h6,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }

      // 自動更新設定セクション
      Text(
        text = "タイムライン自動更新",
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
      )

      // 自動更新のOn/Offチェックボックス
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Checkbox(
          checked = autoRefreshEnabled,
          onCheckedChange = onAutoRefreshToggle
        )
        Text(
          text = "タイムラインを自動的に更新する",
          modifier = Modifier
            .padding(start = 8.dp)
            .clickableNoRipple {
              onAutoRefreshToggle(!autoRefreshEnabled)
            }
        )
      }

      // 更新間隔選択ドロップダウン
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Text(
          text = "更新間隔: ",
          modifier = Modifier.padding(start = 32.dp, end = 8.dp)
        )
        Box {
          Row(
            modifier = Modifier
              .clickable { expanded.value = true }
              .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // 現在選択されている間隔を表示
            val intervalText = autoRefreshIntervalToText(autoRefreshInterval)
            Text(text = intervalText)
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.padding(start = 4.dp)
            )
          }

          DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
          ) {
            AppConstants.AUTO_REFRESH_INTERVALS.forEach { interval ->
              val intervalText = autoRefreshIntervalToText(interval)
              DropdownMenuItem(
                onClick = {
                  onAutoRefreshIntervalChange(interval)
                  expanded.value = false
                }
              ) {
                Text(text = intervalText)
              }
            }
          }
        }
      }

      // タイムライン表示設定セクション
      Text(
        text = "タイムライン表示",
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
      )

      // 表示行数選択ドロップダウン
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Text(
          text = "表示行数: ",
          modifier = Modifier.padding(start = 32.dp, end = 8.dp)
        )
        Box {
          Row(
            modifier = Modifier
              .clickable { linesExpanded.value = true }
              .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // 現在選択されている行数を表示
            Text(text = "${timelineVisibleLines}行")
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.padding(start = 4.dp)
            )
          }

          DropdownMenu(
            expanded = linesExpanded.value,
            onDismissRequest = { linesExpanded.value = false }
          ) {
            AppConstants.TIMELINE_VISIBLE_LINES.forEach { lines ->
              DropdownMenuItem(
                onClick = {
                  onTimelineVisibleLinesChange(lines)
                  linesExpanded.value = false
                }
              ) {
                Text(text = "${lines}行")
              }
            }
          }
        }
      }

      // ツールチップ表示チェックボックス
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Checkbox(
          checked = tooltipEnabled,
          onCheckedChange = onTooltipEnabledChange,
          modifier = Modifier
        )
        Text(
          text = "ツールチップ表示(投稿詳細のポップアップ表示)",
          modifier = Modifier
            .padding(start = 8.dp)
            .clickableNoRipple {
              onTooltipEnabledChange(!tooltipEnabled)
            }
        )
      }

      Spacer(modifier = Modifier.size(16.dp))

      // 未認証なら認証ボタン表示
      if (!authorized) {
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Button(
            onClick = onShowAuthDialog
          ) {
            Text("認証画面を開く")
          }
        }
      }

      // 認証済みなら再認証ボタン表示
      if (authorized) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = onShowAuthDialog,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text("再認証")
          }

          Button(
            onClick = { showConfirmDialog.value = true },
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Text("アカウント削除")
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
      ) {
        Button(
          onClick = { onDismiss() }
        ) {
          Text("OK")
        }
      }
    }
  }

  // 削除確認ダイアログ
  if (showConfirmDialog.value) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog.value = false },
      title = { Text("アカウント削除") },
      text = { Text("アカウント「@$accountScreenName」を削除しますか？") },
      confirmButton = {
        Button(
          onClick = {
            showConfirmDialog.value = false
            onDeleteAccount()
          }
        ) {
          Text("削除")
        }
      },
      dismissButton = {
        Button(
          onClick = { showConfirmDialog.value = false }
        ) {
          Text("キャンセル")
        }
      }
    )
  }
}

fun autoRefreshIntervalToText(autoRefreshInterval: Int) = when (autoRefreshInterval) {
  60 -> "60秒"
  90 -> "90秒"
  120 -> "2分"
  300 -> "5分"
  600 -> "10分"
  else -> "${autoRefreshInterval}秒"
}
