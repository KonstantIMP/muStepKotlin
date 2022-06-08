pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = ("mustep")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app"
)
