package jp.takke.tweenb.app.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun Tab(
  tabNames: List<String>,
  selectedTabIndex: Int,
  onTabSelected: (Int) -> Unit
) {
  var tabHeight by remember { mutableStateOf(0.dp) }
  val tabBorderColor = Color.LightGray
  Divider(color = tabBorderColor)
  Row(
    modifier = Modifier
      .onSizeChanged {
        tabHeight = it.height.dp
      }
      .fillMaxWidth()
  ) {
    tabNames.forEachIndexed { index, it ->
      Text(
        text = it,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
          .background(
            color =
              if (index == selectedTabIndex) Color.White else Color(0xFFE0E0E0)
          )
          .clickable {
            onTabSelected(index)
          }
          .padding(8.dp)
      )

      // Vertical Divider
      VerticalDivider(
        height = tabHeight,
        color = tabBorderColor
      )
    }
  }

}
