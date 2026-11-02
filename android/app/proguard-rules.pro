# ProGuard / R8 rules for Jajan Cuan

# Retain the application class
-keep class com.henrisusanto.vitmate.JajanCuanApplication { *; }

# Retain generated code for kotlinx.serialization (narrowed)
-keep class kotlinx.serialization.KSerializer { *; }
-keepnames class kotlinx.serialization.KSerializer { *; }
-keepclassmembernames class kotlinx.serialization.KSerializer {
    <fields>;
}

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
