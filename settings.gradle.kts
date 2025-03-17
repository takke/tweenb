pluginManagement {
  repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
    google()
    mavenCentral()
  }
}

rootProject.name = "tweenb"
include("app")
