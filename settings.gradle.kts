pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

includeBuild("gradle-plugins/convention-plugins")
rootProject.name = "rune-kt"
include("core")
include("samples")

enableFeaturePreview("VERSION_CATALOGS")