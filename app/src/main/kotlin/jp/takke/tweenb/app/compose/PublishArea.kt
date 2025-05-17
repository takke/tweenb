package jp.takke.tweenb.app.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.takke.tweenb.app.domain.ImageAttachment
import jp.takke.tweenb.app.util.ClipboardUtil

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PublishArea(
  initialText: String = "",
  onTextChange: (String) -> Unit = {},
  onPost: (String) -> Unit = {},
  attachedImages: List<ImageAttachment> = emptyList(),
  onImageAttached: (ImageBitmap) -> Unit = {},
  onImageRemoved: (Int) -> Unit = {}
) {
  val postText = remember {
    mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
  }

  // 入力テキストが外部から変更された場合に反映
  if (postText.value.text != initialText && initialText.isEmpty()) {
    postText.value = TextFieldValue(text = initialText, selection = TextRange(initialText.length))
  }

  val focusRequester = remember { FocusRequester() }

  // ダイアログを表示した時にフォーカスを設定
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(0.dp)
  ) {
    // 画像が添付されている場合はプレビューを表示
    if (attachedImages.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp, vertical = 4.dp)
      ) {
        attachedImages.forEachIndexed { index, attachment ->
          Box(
            modifier = Modifier
              .size(60.dp)
              .padding(4.dp)
              .border(1.dp, Color.Gray)
          ) {
            Image(
              bitmap = attachment.image,
              contentDescription = "Attached image",
              modifier = Modifier.fillMaxSize()
            )

            // 削除ボタン
            IconButton(
              onClick = { onImageRemoved(index) },
              modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .padding(2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove image",
                tint = Color.Gray
              )
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      CustomTextField(
        textFieldValue = postText,
        onValueChange = { text ->
          // 入力テキストの変更をViewModelに通知
          onTextChange(text)
        },
        singleLine = true,
        leadingIcon = null,
        trailingIcon = null,
        style = TextStyle(
          fontSize = 13.sp,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
          onDone = {
            // POST実行
            treatPost(postText, onPost)
          }
        ),
        modifier = Modifier
          .weight(1f)
          .padding(start = 4.dp, end = 8.dp)
          .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
          .padding(8.dp)
          .focusRequester(focusRequester)
          .onKeyEvent { event ->
            // Ctrl+Vが押された場合
//            println("KeyEvent: ${event.type}, Ctrl: ${event.isCtrlPressed}, Key: ${event.key}")
            // なぜか {KeyDown, Ctrl, V} が飛んでこないので {KeyUp, Ctrl, V} で判定
            if (event.type == KeyEventType.KeyUp &&
              event.isCtrlPressed &&
              event.key == Key.V
            ) {
              // クリップボードから画像を取得
//              println("Ctrl+V pressed")
              val image = ClipboardUtil.getImageFromClipboard()
              if (image != null) {
                // 画像を添付
                onImageAttached(image)
                return@onKeyEvent true
              }
            }
            false
          }
      )

      Button(
        onClick = {
          treatPost(postText, onPost)
        }
      ) {
        Text("Post")
      }
    }
  }
}

private fun treatPost(postText: MutableState<TextFieldValue>, onPost: (String) -> Unit) {
  if (postText.value.text.isNotEmpty()) {
    onPost(postText.value.text)
    // onPostコールバックにテキストのクリアは任せる
  }
}

/**
 * 標準の TextField は余白が大きすぎるので調整する
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CustomTextField(
  textFieldValue: MutableState<TextFieldValue>,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  isError: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  onValueChange: (String) -> Unit,
  modifier: Modifier,
  style: TextStyle
) {
  BasicTextField(
    value = textFieldValue.value,
    modifier = modifier,
    onValueChange = { newValue ->
      textFieldValue.value = newValue   // テキストの変更をStateに反映
      onValueChange(newValue.text)
    },
    enabled = enabled,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    interactionSource = interactionSource,
    singleLine = singleLine,
    textStyle = style
  ) { innerTextField ->
    TextFieldDefaults.TextFieldDecorationBox(
      value = textFieldValue.value.text,
      innerTextField = innerTextField,
      enabled = enabled,
      singleLine = singleLine,
      visualTransformation = visualTransformation,
      interactionSource = interactionSource,
//      label = { Text(label) },
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      colors = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = Color.Transparent,
      ),
      // Paddingを調整
      contentPadding = PaddingValues(0.dp)// TextFieldDefaults.textFieldWithoutLabelPadding(0.dp)
    )
  }
}
