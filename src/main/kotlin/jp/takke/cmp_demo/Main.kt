package jp.takke.cmp_demo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

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

fun main() = application {

  val state by remember { mutableStateOf(WindowState()) }

//  LaunchedEffect(Unit) {
//    delay(3_000)
//
//    println("update")
//    state.position = state.position.apply {
//      println("position[$x, $y]")
//      WindowPosition(x + 1000.dp, y)
//    }
//    state.size = state.size.apply {
//      println("size[$width, $height]")
//      DpSize(width + 100.dp, height + 100.dp)
//    }
//
////    state.placement = WindowPlacement.Maximized
//  }

  Window(
    onCloseRequest = ::exitApplication,
    title = "demo of CMP",
    state = state,
  ) {
    AppScreen()
  }
}
