@file:Incubating

rootProject.name = "AeroDex"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":shared:presentation:ui")
include(":shared:presentation:viewmodels")
include(":shared:core:common")
include(":shared:core:di")
include(":shared:core:viewmodels")
include(":shared:core:ui")
include(":shared:domain:usecases")
include(":shared:domain:models")
include(":shared:domain:repository")
include(":shared:data:repository")
include(":shared:data:models")
include(":shared:data:datasources")
include(":shared:data:datasources-core")