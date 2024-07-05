import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("org.sonarqube")
    id("jacoco")
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
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
    testOptions.unitTests {
        isReturnDefaultValues = true
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

jacoco {
    toolVersion = "0.8.8"
}

sonarqube {
    properties {
        property("sonar.projectKey", "KotlinCICDPractice")
        property("sonar.organization", "KotlinCICDPractice")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.login", "318109bb61c534e7a847069ad286b3845dd5b0fb")

        property("sonar.sources", "src/main/java")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.tests", "src/test/java")
//        property("sonar.test.inclusions", "**/*Test*/**")
//        property(
//            "sonar.exclusions",
//            "**/*Test*/**," +
//                "*.json," +
//                "**/*test*/**," +
//                "**/.gradle/**," +
//                "**/R.class",
//        )

        property("sonar.android.lint.reportPaths", "build/reports/lint-results.xml")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.junit.reportPaths", "build/test-results/testDebugUnitTest")
        property("sonar.jacoco.reportPaths", "**/jacoco/*.exec")
//        property("sonar.jacoco.reportPaths", "build/jacoco/testDebugUnitTest.exec")
//        property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property(
            "sonar.kotlin.ktlint.reportPaths",
            "build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.xml"
        )
    }
}

tasks.withType<Test> {
    useJUnit()
    finalizedBy(tasks.named("jacocoTestReport"))
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = arrayListOf("jdk.internal.*")       //This line
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(file("build/reports/jacoco"))
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
        )

    val debugTree =
        fileTree("${layout.buildDirectory}/intermediates/classes/debug") {
            exclude(fileFilter)
        }
    val kotlinDebugTree =
        fileTree("${layout.buildDirectory}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/code-coverage/connected/*coverage.ec",
            )
        },
    )
}
