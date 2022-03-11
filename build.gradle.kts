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


val hash: String by lazy {
    val stdout = java.io.ByteArrayOutputStream()
    rootProject.exec {
        commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
        standardOutput = stdout
    }
    stdout.toString().trim()
}
allprojects {
    repositories {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenCentral()
        mavenLocal()
    }

    group = "com.lehaine.rune"

    val isReleaseVersion = !runeVersion.endsWith("SNAPSHOT")
    version = if (isReleaseVersion) {
        runeVersion
    } else {
        "$runeVersion#$hash"
    }

    extra["isReleaseVersion"] = isReleaseVersion
}