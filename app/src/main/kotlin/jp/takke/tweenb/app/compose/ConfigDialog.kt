package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfigDialog(
  showConfigDialog: Boolean,
  onDismiss: () -> Unit,
  onShowAuthDialog: () -> Unit,
  onDeleteAccount: () -> Unit,
  accountScreenName: String?,
) {
  if (!showConfigDialog) {
    return
  }

  val showConfirmDialog = remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colors.background)
        .padding(16.dp)
        .width(400.dp)
        .height(300.dp)
    ) {
      val authorized = accountScreenName != null && accountScreenName.isNotEmpty()

      // アカウント情報表示
      if (authorized) {
        Text(
          text = "現在のアカウント: @$accountScreenName",
          style = MaterialTheme.typography.h6,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }

      Spacer(modifier = Modifier.weight(1f))

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
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Button(
            onClick = onShowAuthDialog
          ) {
            Text("再認証")
          }
        }

        // アカウント削除ボタン表示
        Spacer(modifier = Modifier.height(16.dp))
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Button(
            onClick = { showConfirmDialog.value = true }
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
