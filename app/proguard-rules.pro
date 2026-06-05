# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Rule to prevent aggressive minimization / obfuscation of necessary application components
-keep class com.example.** { *; }
-keep interface com.example.** { *; }
-keep enum com.example.** { *; }

# Prevent Room from being stripped or obfuscated
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Prevent Moshi Kotlin and companion codegen from reflection issues
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Prevent Retrofit details from being stripped
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Prevent OkHttp details from being stripped
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

