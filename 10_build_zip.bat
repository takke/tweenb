@echo off

::: clean, zip
call gradlew %MY_GRADLE_OPTIONS% clean test :app:createZipDistribution

pause
