import org.gradle.internal.impldep.org.jsoup.safety.Safelist.basic

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {

            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")

            // Do not change the username below. It should always be "mapbox" (not your username).

            credentials.username = "mapbox"

            // Use the secret token stored in gradle.properties as the password
            credentials.password = "sk.eyJ1IjoiYW5keS1iaW9uaWMiLCJhIjoiY20xNHk3NHNyMDNwMjJqcXZieXplaTlpMSJ9.kvZ1cTUAa_J6NarVpq-PKA"

            authentication.create<BasicAuthentication>("basic")

        }
    }
}

rootProject.name = "FUTMaps"
include(":app")
 