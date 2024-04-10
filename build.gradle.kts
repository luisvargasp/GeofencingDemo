// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false


    id ("com.google.dagger.hilt.android") version "2.44" apply false
    id ("androidx.navigation.safeargs.kotlin") version "2.5.3" apply false
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

    //id("com.google.dagger.hilt.android") version "2.48.1" apply false
    //id("com.google.gms.google-services") version "4.4.0" apply false
}