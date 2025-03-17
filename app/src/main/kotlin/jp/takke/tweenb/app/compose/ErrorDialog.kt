package jp.takke.tweenb.app.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
  // クリップボード管理
  val clipboardManager = LocalClipboardManager.current

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
        Row(
          modifier = Modifier.padding(bottom = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "スタックトレース:",
            style = MaterialTheme.typography.subtitle1
          )

          Spacer(modifier = Modifier.weight(1f))

          // コピーボタン
          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(stackTrace))
            }
          ) {
            Text(
              text = "コピー"
            )
          }
        }

        // スクロール可能なスタックトレース表示
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        ) {
          TextField(
            value = stackTrace,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
              .fillMaxSize(),
            colors = TextFieldDefaults.textFieldColors(
              backgroundColor = MaterialTheme.colors.surface,
              disabledTextColor = MaterialTheme.colors.onSurface,
              disabledLabelColor = MaterialTheme.colors.onSurface
            ),
            textStyle = MaterialTheme.typography.body2
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