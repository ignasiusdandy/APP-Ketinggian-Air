# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep App Models (so GSON can map JSON keys to fields)
-keep class com.baingat.app.**Model { *; }
-keep class com.baingat.app.**Model$** { *; }
-keep class com.baingat.app.**Response { *; }
-keep class com.baingat.app.**Response$** { *; }
-keep class com.baingat.app.**Request { *; }
-keep class com.baingat.app.LoginResponse { *; }
-keep class com.baingat.app.LoginResponse$** { *; }
-keep class com.baingat.app.ResponseBody { *; }
-keep class com.baingat.app.ChartItem { *; }
-keep class com.baingat.app.Kendaraan { *; }
-keep class com.baingat.app.OnboardingItem { *; }
-keep class com.baingat.app.RiwayatModel { *; }
-keep class com.baingat.app.WilayahModel { *; }
-keep class com.baingat.app.LokasiModel { *; }
-keep class com.baingat.app.GantiKataSandiModel { *; }
-keep class com.baingat.app.UpdateKalibrasiRequest { *; }
-keep class com.baingat.app.TambahKendaraanAdminRequest { *; }