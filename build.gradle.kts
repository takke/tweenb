import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.compose)
  alias(libs.plugins.kotlin.compose)
}

group = "jp.takke.tweenb"
version = "1.0-SNAPSHOT"

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
}

compose.desktop {
  application {
    mainClass = "jp.takke.tweenb.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "tweenb"
      packageVersion = "1.0.0"
    }
  }
}
