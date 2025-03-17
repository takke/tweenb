package jp.takke.tweenb

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import jp.takke.tweenb.app.AppScreen
import jp.takke.tweenb.app.repository.AppPropertyRepository
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.coroutines.flow.distinctUntilChanged

fun main() = application {
  // Loggerの初期化
  val logger = remember { LoggerWrapper("Main") }

  LaunchedEffect(Unit) {
    logger.i("アプリケーション起動")
  }

  // アプリケーション設定リポジトリ
  val propertyRepository = AppPropertyRepository.instance

  // 保存されていた設定からウィンドウ状態を作成
  val state = rememberWindowState(
    position = propertyRepository.getWindowPosition(),
    size = propertyRepository.getWindowSize()
  )

  LaunchedEffect(Unit) {
    logger.i("ウィンドウ状態: position=${state.position}, size=${state.size}")
  }

  // ウィンドウの位置・サイズ変更
  LaunchedEffect(state) {
    snapshotFlow { state.position }
      .distinctUntilChanged()
      .collect { position ->
        logger.d("ウィンドウ位置変更: $position")
        propertyRepository.saveWindowPosition(position)
      }
  }

  LaunchedEffect(state) {
    snapshotFlow { state.size }
      .distinctUntilChanged()
      .collect { size ->
        logger.d("ウィンドウサイズ変更: $size")
        propertyRepository.saveWindowSize(size)
      }
  }

  // アプリケーション終了時に設定を保存（念のため）
  DisposableEffect(Unit) {
    onDispose {
      logger.i("アプリケーション終了")
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
