val runeVersion: String by project

buildscript {
    repositories {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath(libs.bundles.plugins)
    }
}

allprojects {
    repositories {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenCentral()
        mavenLocal()
    }

    group = "com.lehaine.rune"
    version = runeVersion

    val isReleaseVersion = !runeVersion.endsWith("SNAPSHOT")
    extra["isReleaseVersion"] = isReleaseVersion
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        yarnLockMismatchReport = org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport.WARNING
    }
}