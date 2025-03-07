package jp.takke.cmp_demo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*

@Composable
@Preview
fun AppScreen() {
  var text by remember { mutableStateOf("Hello, World!") }

  MaterialTheme {
    Button(onClick = {
      text = "Hello, Desktop!"
    }) {
      Text(
        text = text,
        style = MaterialTheme.typography.body1
      )
    }
  }
}