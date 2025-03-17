package jp.takke.tweenb.app.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.takke.tweenb.app.domain.BsFeedViewPost
import jp.takke.tweenb.app.domain.ColumnInfo
import jp.takke.tweenb.app.domain.ColumnType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostItem(
  post: BsFeedViewPost,
  modifier: Modifier,
  cols: List<ColumnInfo>,
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
      cols.forEachIndexed { index, columnInfo ->
        when (columnInfo.type) {
          ColumnType.Icon -> {
            // アイコン
            Box(
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp),
              contentAlignment = Alignment.Center
            ) {
              // TODO: 実際のアバター画像を表示する
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .drawWithContent {
                    drawCircle(
                      color = Color.LightGray
                    )
                  }
              )
            }
          }

          ColumnType.Name -> {
            // 名前
            Column(
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp)
            ) {
              Text(
                text = post.post.author?.displayName ?: "",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "@${post.post.author?.handle ?: ""}",
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          ColumnType.Post -> {
            // 投稿内容
            Text(
              text = post.post.record?.asFeedPost?.text ?: "",
              style = MaterialTheme.typography.body1,
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp),
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }

          ColumnType.DateTime -> {
            // 日時
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(post.post.indexedAt)
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.caption,
              modifier = Modifier
                .width(columnInfo.width)
                .padding(8.dp)
            )
          }
        }

        if (index < cols.size - 1) {
          VerticalDivider(
            height = 48.dp,
            color = Color.LightGray,
          )
        }
      }
    }

    Divider(
      color = Color.LightGray
    )
  }
}