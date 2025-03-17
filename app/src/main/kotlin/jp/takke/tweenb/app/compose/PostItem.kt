package jp.takke.tweenb.app.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PostItem(
  index: Int,
  modifier: Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .drawWithContent {
          drawContent()
          drawLine(
            color = Color.LightGray,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 1.dp.toPx()
          )
          drawLine(
            color = Color.LightGray,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
          )
        }
    ) {
      cols.forEach {
        Text(
          text = "${index + 1} ${it.name}",
          style = MaterialTheme.typography.body1,
          modifier = Modifier
            .width(it.width)
            .padding(8.dp)
        )

        VerticalDivider(
          height = 48.dp,
          color = Color.LightGray,
        )
      }
    }

    Divider(
      color = Color.LightGray
    )
  }
}