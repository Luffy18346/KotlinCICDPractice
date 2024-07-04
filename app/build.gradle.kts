import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.example.kotlincicdpractice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kotlincicdpractice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
}

ktlint {
    android = true
    ignoreFailures = false
    verbose = true
    outputToConsole = true
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.SARIF)
        reporter(ReporterType.JSON)
    }
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
}

// StandAlone Lint Checks on Builds
// tasks.preBuild.dependsOn("detekt").dependsOn("ktlintFormat")

// Apply the git-hooks.gradle file
apply(from = "$rootDir/team-props/git-hooks.gradle")

// Ensure clean and build tasks depend on installGitHooks
afterEvaluate {
    tasks.named("clean") {
        dependsOn("makeGradlewExecutable", "installGitHooks")
    }
    tasks.named("build") {
        dependsOn("makeGradlewExecutable", "installGitHooks")
    }
    tasks.named("assemble") {
        dependsOn("makeGradlewExecutable", "installGitHooks")
    }
}