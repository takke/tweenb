package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.takke.tweenb.app.viewmodel.AppViewModel

@Composable
fun AuthDialog(
  showConfigDialog: Boolean,
  onDismiss: () -> Unit,
  onStartAuth: () -> Unit,
  uiState: AppViewModel.UiState,
  onCodeChanged: (String) -> Unit,
) {
  if (!showConfigDialog) {
    return
  }

  Dialog(
    onDismissRequest = {
//      onDismiss()
    },
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
        when (uiState.loginState) {
          AppViewModel.UiState.LoginState.INIT -> {
            // TODO ユーザー名入力があったほうがいい
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              Button(
                onClick = { onStartAuth() }
              ) {
                Text("認証開始")
              }
            }
          }

          AppViewModel.UiState.LoginState.LOADING -> {
            // 処理中
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator()
            }
          }

          AppViewModel.UiState.LoginState.WAITING_CODE -> {
            // コード入力待ち
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              Text("コードを入力してください")
            }

            TextField(
              value = uiState.code,
              onValueChange = { onCodeChanged(it) },
            )
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

      // エラーメッセージ
      if (uiState.validationErrorMessage.isNotEmpty()) {
        Text(
          text = uiState.validationErrorMessage,
          style = MaterialTheme.typography.body2
        )
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
