package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfigDialog(
  showConfigDialog: Boolean,
  onDismiss: () -> Unit,
  onShowAuthDialog: () -> Unit,
) {
  if (!showConfigDialog) {
    return
  }

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
      val authorized = false

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
