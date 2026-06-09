import java.security.KeyStore

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.cardclash.wjzkpa"
    minSdk = 24
    targetSdk = 36
    
    // Static version configuration to support predictable static parsers on publishing platforms
    versionCode = 100
    versionName = "1.4.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystoreExists = file("${rootDir}/my-upload-key.jks").exists()
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: if (keystoreExists) "${rootDir}/my-upload-key.jks" else "${rootDir}/debug.keystore"
      val resolvedStoreFile = file(keystorePath)
      storeFile = resolvedStoreFile
      
      val resolvedStorePassword = System.getenv("STORE_PASSWORD") ?: if (keystoreExists) "" else "android"
      storePassword = resolvedStorePassword
      
      var resolvedKeyAlias = System.getenv("KEY_ALIAS")
      if (resolvedKeyAlias.isNullOrEmpty()) {
          if (resolvedStoreFile.exists()) {
              val keystoreTypes = listOf("JKS", "PKCS12", KeyStore.getDefaultType())
              var loadedAlias: String? = null
              for (type in keystoreTypes) {
                  try {
                      val ks = KeyStore.getInstance(type)
                      resolvedStoreFile.inputStream().use { stream ->
                          ks.load(stream, resolvedStorePassword.toCharArray())
                          val aliases = ks.aliases()
                          if (aliases.hasMoreElements()) {
                              loadedAlias = aliases.nextElement()
                          }
                      }
                      if (loadedAlias != null) break
                  } catch (e: Exception) {
                      // Try next keystore type
                  }
              }
              if (loadedAlias != null) {
                  resolvedKeyAlias = loadedAlias
              }
          }
      }
      if (resolvedKeyAlias.isNullOrEmpty()) {
          resolvedKeyAlias = if (keystoreExists) "upload" else "androiddebugkey"
      }
      
      keyAlias = resolvedKeyAlias
      keyPassword = System.getenv("KEY_PASSWORD") ?: resolvedStorePassword
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
