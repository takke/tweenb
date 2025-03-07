package jp.takke.cmp_demo

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {

//  val state by remember { mutableStateOf(WindowState()) }
  val state = rememberWindowState(
    size = DpSize(600.dp, 600.dp),
  )

//  LaunchedEffect(Unit) {
//    delay(3_000)
//
//    println("update")
////    state.position = state.position.apply {
////      println("position[$x, $y]")
////      WindowPosition(x + 1000.dp, y)
////    }
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
