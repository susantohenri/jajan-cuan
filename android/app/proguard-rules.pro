# ProGuard / R8 rules for Jajan Cuan

# Retain the application class
-keep class com.henrisusanto.vitmate.JajanCuanApplication { *; }

# Retain generated code for kotlinx.serialization
-keep class kotlinx.serialization.** { *; }
-keepnames class kotlinx.serialization.** { *; }
-keepclassmembernames class kotlinx.serialization.KSerializer { *; }

# Retain Room annotations and generated classes
-keep class androidx.room.** { *; }

# Retain AdMob & UMP classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# Retain Firebase classes
-keep class com.google.firebase.** { *; }

# Keep enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
