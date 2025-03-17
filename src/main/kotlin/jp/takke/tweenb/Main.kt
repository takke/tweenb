package jp.takke.tweenb

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
  // アプリケーション設定リポジトリ
  val propertyRepository = AppPropertyRepository()

  // 保存されていた設定からウィンドウ状態を作成
  val state = rememberWindowState(
    position = propertyRepository.getWindowPosition(),
    size = propertyRepository.getWindowSize()
  )

  // アプリケーション終了時に設定を保存
  LaunchedEffect(Unit) {
    // アプリケーション終了時に実行されるシャットダウンフック
    Runtime.getRuntime().addShutdownHook(Thread {
      propertyRepository.saveWindowState(state.position, state.size)
    })
  }

  Window(
    onCloseRequest = ::exitApplication,
    title = "tweenb",
    state = state,
  ) {
    AppScreen()
  }
}
