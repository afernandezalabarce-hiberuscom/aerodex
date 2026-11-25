import org.gradle.api.JavaVersion

object BuildVersion {
    object environment {
        const val applicationId = "dev.afalabarce.aircrafts"
        const val appName = "AeroDex"
        const val appVersion = 100
        const val appVersionCode = "1.0.0"
        val javaVersion = JavaVersion.VERSION_17
        const val jvmTarget = "17"
        const val appDatabaseName = "AeroDexDatabase"
    }

    object android {
        const val minSdk = 26
        const val compileSdk = 37
        const val compileSdkMinor = 1
    }
}