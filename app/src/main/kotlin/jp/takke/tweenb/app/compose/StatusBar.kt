package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jp.takke.tweenb.app.viewmodel.AppViewModel

@Composable
fun StatusBar(
  appViewModel: AppViewModel,
  uiState: AppViewModel.UiState
) {
  val statusText = if (appViewModel.blueskyClientInitialized) {
    buildString {
      append("@${appViewModel.account?.screenName}")
      if (uiState.autoRefreshEnabled) {
        val intervalText = autoRefreshIntervalToText(uiState.autoRefreshInterval)
        append(" [間隔: $intervalText]")
      }
    }
  } else {
    "未認証"
  }
  Text(
    text = statusText,
    style = MaterialTheme.typography.body1,
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.LightGray)
      .padding(8.dp)
  )
}