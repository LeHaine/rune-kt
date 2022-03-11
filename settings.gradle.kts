pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("gradle-plugins/convention-plugins")
rootProject.name = "rune-kt"
include("core")

enableFeaturePreview("VERSION_CATALOGS")