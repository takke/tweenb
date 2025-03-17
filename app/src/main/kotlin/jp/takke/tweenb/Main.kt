package jp.takke.tweenb

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import jp.takke.tweenb.app.AppScreen
import jp.takke.tweenb.app.repository.AppPropertyRepository
import kotlinx.coroutines.flow.distinctUntilChanged

fun main() = application {
  // アプリケーション設定リポジトリ
  val propertyRepository = AppPropertyRepository.instance

  // 保存されていた設定からウィンドウ状態を作成
  val state = rememberWindowState(
    position = propertyRepository.getWindowPosition(),
    size = propertyRepository.getWindowSize()
  )

  // ウィンドウの位置やサイズが変更されたときにも保存する
  LaunchedEffect(state) {
    // ウィンドウの位置の変更を監視
    snapshotFlow { state.position }
      .distinctUntilChanged()
      .collect { position ->
        propertyRepository.saveWindowPosition(position)
      }
  }

  LaunchedEffect(state) {
    // ウィンドウのサイズの変更を監視
    snapshotFlow { state.size }
      .distinctUntilChanged()
      .collect { size ->
        propertyRepository.saveWindowSize(size)
      }
  }

  // アプリケーション終了時に設定を保存（念のため）
  DisposableEffect(Unit) {
    onDispose {
      propertyRepository.saveWindowState(state.position, state.size)
    }
  }

  Window(
    onCloseRequest = ::exitApplication,
    title = "tweenb",
    state = state,
  ) {
    AppScreen()
  }
}
