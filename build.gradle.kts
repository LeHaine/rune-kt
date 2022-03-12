val runeVersion: String by project

buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.bundles.plugins)
    }
}



allprojects {
    repositories {
        mavenLocal()
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenCentral()
    }

    group = "com.lehaine.rune"
    version = runeVersion

    val isReleaseVersion = !runeVersion.endsWith("SNAPSHOT")
    extra["isReleaseVersion"] = isReleaseVersion
}