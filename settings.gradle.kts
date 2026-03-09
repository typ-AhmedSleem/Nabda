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
include(":core")
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