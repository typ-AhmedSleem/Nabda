pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nabda"
include(":app")
include(":core:common")
include(":core:model")
include(":core:gestures")
include(":core:actions")
include(":core:dispatcher")
include(":core:messaging")
include(":core:notifications")
include(":core:pairing")
include(":feature:deafblind")
include(":feature:caregiver")
include(":feature:pairing")
include(":infrastructure:infra-fcm")
include(":infrastructure:infra-storage")
include(":infrastructure:infra-qr")
include(":infrastructure:infra-localnetwork")
include(":designsystem")
include(":caregiver-app")
include(":deafblind-app")