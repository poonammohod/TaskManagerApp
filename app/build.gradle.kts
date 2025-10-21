import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pk.taskmanagerapp"
    compileSdk = 34  // Changed from 36 to 34 (latest stable)

    defaultConfig {
        applicationId = "com.pk.taskmanagerapp"
        minSdk = 24
        targetSdk = 34  // Changed from 36 to 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase - Use only ONE BOM version
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Additional
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

//import org.gradle.kotlin.dsl.implementation
//
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
//    id 'com.android.application'
//    id 'org.jetbrains.kotlin.android'
//    id 'com.google.gms.google-services'
//}
//
//android {
//    namespace = "com.pk.taskmanagerapp"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.pk.taskmanagerapp"
//        minSdk = 24
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//    buildFeatures {
//        compose = true
//        viewBinding = true
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation 'androidx.core:core-ktx:1.12.0'
//    implementation 'androidx.appcompat:appcompat:1.6.1'
//    implementation 'com.google.android.material:material:1.10.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
//    implementation 'androidx.fragment:fragment-ktx:1.6.2'
//    implementation 'androidx.recyclerview:recyclerview:1.3.2'
//    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
//    implementation 'androidx.viewpager2:viewpager2:1.0.0'
//
//    // Firebase
//    implementation platform('com.google.firebase:firebase-bom:32.5.0')
//    implementation 'com.google.firebase:firebase-auth-ktx'
//    implementation 'com.google.firebase:firebase-firestore-ktx'
//    implementation 'com.google.firebase:firebase-database-ktx'
//
//    // Additional
//    implementation 'com.github.bumptech.glide:glide:4.14.2'
//    implementation 'de.hdodenhof:circleimageview:3.1.0'
//    implementation 'androidx.cardview:cardview:1.0.0'
//}