package jp.takke.tweenb.app.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

/**
 * エラーダイアログ
 *
 * @param show ダイアログを表示するかどうか
 * @param onDismiss ダイアログを閉じる時のコールバック
 * @param errorMessage エラーメッセージ
 * @param stackTrace スタックトレース
 */
@Composable
fun ErrorDialog(
  show: Boolean,
  onDismiss: () -> Unit,
  errorMessage: String,
  stackTrace: String
) {
  // エラーメッセージ
  val state = rememberDialogState(
    size = DpSize(800.dp, 600.dp)
  )
  DialogWindow(
    visible = show,
    state = state,
    onCloseRequest = onDismiss,
    title = "エラー",
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colors.background
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .fillMaxSize()
      ) {
        // エラーメッセージ
        Text(
          text = errorMessage,
          style = MaterialTheme.typography.h6,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        // スタックトレース
        Text(
          text = "スタックトレース:",
          style = MaterialTheme.typography.subtitle1,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        // スクロール可能なスタックトレース表示
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        ) {
          Text(
            text = stackTrace,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
              .padding(8.dp)
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
          )
        }

        // 閉じるボタン
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onDismiss
          ) {
            Text("閉じる")
          }
        }
      }
    }
  }
}