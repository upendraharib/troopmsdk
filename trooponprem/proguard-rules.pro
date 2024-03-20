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
-keep class org.mediasoup.droid.** { *; }
-keep class org.webrtc.** { *; }
-keep class net.zetetic.**{ *; }
-keep class org.spongycastle.** {*;}
-keep class retrofit2.** { *; }
-keep interface retrofit2.** {*;}
-keep class okio.** { *; }
-keep interface okio.** {*;}
-keep class io.reactivex.** { *; }
-keep class com.rajat.pdfviewer.** {*;}
-keep class javax.naming.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class androidx.annotation.** {*;}
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}
-keep class com.google.gson.**{*;}
-keep class com.google.gson.annotations.** { *; }
-keep class com.google.gson.reflect.** { *; }
-keepclasseswithmembernames class * { native<methods>; }
-keep class android.widget.Toast {
    *;
}
-keepclassmembers class android.widget.Toast {
    *;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
