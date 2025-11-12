pluginManagement {
    repositories {
        // Essayer Maven Central en premier
        mavenCentral()
        gradlePluginPortal()
        
        // Google en second
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        
        // Miroir Maven en cas de problème
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Essayer Maven Central en premier
        mavenCentral()
        
        // Google en second
        google()
        
        // Miroirs de secours
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "kumbaka"
include(":app")
