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
        .width(300.dp)
    ) {

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
