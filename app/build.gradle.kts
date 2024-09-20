android.buildFeatures.buildConfig = true

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20-RC"
    //kotlin("plugin.serialization") version "1.6.21"
    id("com.google.gms.google-services")

    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.andyslab.futmaps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.andyslab.futmaps"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "ALGOLIA_WRITE_KEY",
            "\"${project.findProperty("ALGOLIA_WRITE_KEY") ?: ""}\"")

        buildConfigField("String", "ALGOLIA_APP_ID",
            "\"${project.findProperty("ALGOLIA_APP_ID") ?: ""}\"")

        buildConfigField("String", "ALGOLIA_SEARCH_KEY",
            "\"${project.findProperty("ALGOLIA_SEARCH_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation(libs.androidx.constraintlayout)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //System Bars Colors
    runtimeOnly("com.google.accompanist:accompanist-systemuicontroller:0.35.1-alpha")

    //Viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    //Morfly's Bottom Sheet
    implementation("io.morfly.compose:advanced-bottomsheet-material3:0.1.0")

    //Firebase
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0-RC") //Firebase coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0-RC")

    //Compose Navigation
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    //Mapbox Maps
    implementation("com.mapbox.maps:android:11.5.0")
    implementation("com.mapbox.extension:maps-compose:11.5.0")

    //Mapbox Navigation & Copilot
    implementation("com.mapbox.navigationcore:navigation:3.3.0-rc.1")
    implementation("com.mapbox.navigationcore:copilot:3.3.0-rc.1")
    implementation("com.mapbox.navigationcore:ui-maps:3.3.0-rc.1")
    implementation("com.mapbox.navigationcore:tripdata:3.3.0-rc.1")
    implementation("com.mapbox.navigationcore:ui-components:3.3.0-rc.1")

    //Algolia
    implementation("com.algolia:algoliasearch-client-kotlin:2.0.0")

    //Okhttp
    implementation("io.ktor:ktor-client-android:2.0.1")

    //Preferences Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //Gson
    implementation ("com.google.code.gson:gson:2.10.1")

    //Gson Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Google Play Locations Provider
    implementation("com.google.android.gms:play-services-location:21.0.1")
}