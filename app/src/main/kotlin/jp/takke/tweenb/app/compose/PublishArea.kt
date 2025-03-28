package jp.takke.tweenb.app.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PublishArea(
  initialText: String = "",
  onTextChange: (String) -> Unit = {},
  onPost: (String) -> Unit = {}
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
