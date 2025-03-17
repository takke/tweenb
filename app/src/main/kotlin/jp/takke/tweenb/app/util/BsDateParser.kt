package jp.takke.tweenb.app.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object BsDateParser {

  fun parseDate(dateText: String?): Date? {
    if (dateText == null) {
      return null
    }

    return try {
      when {
        // 2023-12-17T06:30:00.000110+00:00
        dateText.matches(".*\\.\\d{6}\\+\\d{2}:\\d{2}".toRegex()) -> {
          dateFormat0.parse(dateText)
        }

        // 2024-02-14T13:25:30.755+09:00
        dateText.matches(".*\\.\\d{3}\\+\\d{2}:\\d{2}".toRegex()) -> {
          dateFormat2a.parse(dateText)
        }

        dateText.endsWith("Z") -> {
          when (dateText.length) {
            24 -> {
              // 2023-12-16T13:16:57.389Z
              dateFormat1s3.parse(dateText)
            }

            27 -> {
              // 2024-01-02T04:26:55.170831Z
              dateFormat1s6.parse(dateText)
            }

            else -> {
              // 2023-12-16T13:16:57Z
              dateFormat1s0.parse(dateText)
            }
          }
        }

        else -> {
          // 2023-12-16T14:50:08+09:00 ISO 8601 拡張
          dateFormat2.parse(dateText)
        }
      }
    } catch (e: ParseException) {
      null
    }
  }

  private val dateFormat0 by lazy {
    // 2023-12-17T06:30:00.000110+00:00
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.US)
  }

  private val dateFormat1s0 by lazy {
    // 2023-12-16T13:16:57Z
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).also {
      it.timeZone = TimeZone.getTimeZone("GMT")
    }
  }

  private val dateFormat1s3 by lazy {
    // 2023-12-16T13:16:57.389Z
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).also {
      it.timeZone = TimeZone.getTimeZone("GMT")
    }
  }

  private val dateFormat1s6 by lazy {
    // 2024-01-02T04:26:55.170831Z
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).also {
      it.timeZone = TimeZone.getTimeZone("GMT")
    }
  }

  private val dateFormat2 by lazy {
    // 2023-12-16T14:50:08+09:00
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
  }

  private val dateFormat2a by lazy {
    // 2024-02-14T13:25:30.755+09:00
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
  }

}

