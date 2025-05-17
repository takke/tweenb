package jp.takke.tweenb.app.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.takke.tweenb.app.domain.ImageAttachment

@Composable
fun ConfirmPostDialog(
  show: Boolean,
  postText: String,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  attachedImages: List<ImageAttachment> = emptyList()
) {
  if (!show) return

  val focusRequester = remember { FocusRequester() }

  Dialog(
    onDismissRequest = onDismiss
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      elevation = 8.dp,
      modifier = Modifier
        .onPreviewKeyEvent { keyEvent ->
          // Enterキーで投稿を実行
          if (keyEvent.type == KeyEventType.KeyDown &&
            keyEvent.key == Key.Enter &&
            !keyEvent.isShiftPressed
          ) {
            onConfirm()
            true
          } else {
            false
          }
        }
        .focusRequester(focusRequester)
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth()
      ) {
        // タイトル
        Text(
          text = "投稿確認",
          style = MaterialTheme.typography.h6,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 投稿内容
        Text(
          text = "以下の内容で投稿します：",
          style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 投稿内容を表示するカード
        Card(
          modifier = Modifier.fillMaxWidth(),
          elevation = 2.dp
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // テキスト部分
            Text(
              text = postText,
              style = MaterialTheme.typography.body1
            )

            // 画像がある場合はプレビューを表示
            if (attachedImages.isNotEmpty()) {
              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "添付画像:",
                style = MaterialTheme.typography.caption
              )

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth()
              ) {
                attachedImages.forEach { attachment ->
                  Box(
                    modifier = Modifier
                      .size(64.dp)
                      .padding(4.dp)
                      .border(1.dp, Color.Gray)
                  ) {
                    Image(
                      bitmap = attachment.image,
                      contentDescription = attachment.alt,
                      modifier = Modifier.fillMaxSize()
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ボタン
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onDismiss
          ) {
            Text("キャンセル")
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onConfirm
          ) {
            Text("投稿する")
          }
        }
      }
    }

    // ダイアログを表示した時にフォーカスを設定
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
  }
} 