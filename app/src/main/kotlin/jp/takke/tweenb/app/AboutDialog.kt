package jp.takke.tweenb.app

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
internal fun AboutDialog(showAboutDialog: Boolean, onDismiss: () -> Unit) {
  if (!showAboutDialog) {
    return
  }

  Dialog(
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .padding(16.dp)
        .width(300.dp)
    ) {
      Text(
        text = "tweenb",
        style = MaterialTheme.typography.h6
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "バージョン: 0.1.0",
        style = MaterialTheme.typography.body1
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Copyright © 2025 Hiroaki TAKEUCHI",
        style = MaterialTheme.typography.body2
      )
      Spacer(modifier = Modifier.height(16.dp))
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
      ) {
        Button(
          onClick = { onDismiss() }
        ) {
          Text("閉じる")
        }
      }
    }
  }
}