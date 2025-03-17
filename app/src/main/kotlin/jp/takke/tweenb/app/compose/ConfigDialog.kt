package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfigDialog(
  showConfigDialog: Boolean,
  onDismiss: () -> Unit,
) {
  if (!showConfigDialog) {
    return
  }

  Dialog(
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .padding(16.dp)
        .width(400.dp)
        .height(300.dp)
    ) {
      val authorized = false

      Spacer(modifier = Modifier.weight(1f))

      // 未認証なら認証ボタン表示
      if (!authorized) {
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Button(
            onClick = { /* 認証開始処理 */ }
          ) {
            Text("認証")
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
            onClick = { /* 再認証開始処理 */ }
          ) {
            Text("再認証")
          }
        }

        // TODO アカウント削除ボタン表示
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
}
