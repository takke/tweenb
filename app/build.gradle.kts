import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.compose)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

group = "jp.takke.tweenb"
version = "1.0-SNAPSHOT"

// バージョン情報を参照できるように Version.kt を生成する
kotlin {
  sourceSets.main {
    kotlin.srcDir("build/generated/kotlin")
  }
}

tasks.register("generateVersionKt") {
  val dir = file("build/generated/kotlin/jp/takke/tweenb")
  outputs.dir(dir)

  doLast {
    dir.mkdirs()
    file("$dir/Version.kt").writeText(
      """
            package jp.takke.tweenb
            
            object Version {
                const val VERSION = "${project.version}"
            }
        """.trimIndent()
    )
  }
}

tasks.named("compileKotlin") {
  dependsOn("generateVersionKt")
}

dependencies {
  // Note, if you develop a library, you should use compose.desktop.common.
  // compose.desktop.currentOs should be used in launcher-sourceSet
  // (in a separate module for demo project and in testMain).
  // With compose.desktop.common you will also lose @Preview functionality
  implementation(compose.desktop.currentOs)

  // JetBrains Compose
  implementation(libs.compose.ui.ui)
  implementation(libs.compose.ui.tooling)
  implementation(libs.compose.material.material)
  implementation(libs.compose.runtime)

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

  // Lifecycle
  implementation(libs.lifecycle.viewmodel)

  // Bluesky/ATProtocolライブラリ
  implementation("work.socialhub.kbsky:core:0.3.0")
  implementation("work.socialhub.kbsky:auth:0.3.0")
  implementation(libs.cryptography.core)
  implementation(libs.cryptography.jdk)

  // Kotlinx Serialization
  implementation(libs.kotlinx.serialization.json)
}

compose.desktop {
  application {
    mainClass = "jp.takke.tweenb.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "tweenb"
      packageVersion = "1.0.0"
      
      // アプリケーション情報
      vendor = "Hiroaki TAKEUCHI"
      copyright = "© 2025 Hiroaki TAKEUCHI. All rights reserved."
      description = "Bluesky client for desktop"
      
      // Windows MSI設定
      windows {
        // スタートメニューへの追加
        menu = true
        // デスクトップショートカットの作成
        shortcut = true
        // アップグレード時にUUIDを維持
        upgradeUuid = "2A06E9F3-8F9B-4A58-9B95-6D878D7C4C30"
        // メニュー名
        menuGroup = "tweenb"
      }
    }
  }
} 