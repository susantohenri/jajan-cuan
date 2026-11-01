<?xml version="1.0" encoding="utf-8"?>
<proguard-rules.pro>
    # Add project specific ProGuard rules here.
    # By default, the flags in this file are appended to rules specified
    # in /Users/webhe/Downloads/jajan-cuan/android/app/build.gradle.kts
    # You can edit this file to enable specific optimizations.

    # Retain the application class
    -keep class com.henrisusanto.vitmate.JajanCuanApplication { *; }

    # Retain generated code for kotlinx.serialization
    -keep class kotlinx.serialization.** { *; }
    -keepnames class kotlinx.serialization.** { *; }

    # Retain Room annotations
    -keep class androidx.room.** { *; }

    # Retain Model classes
    -keep class com.henrisusanto.vitmate.data.model.** { *; }

    # Retain AdMob classes
    -keep class com.google.android.gms.ads.** { *; }
    -keep class com.google.android.ump.** { *; }

    # Retain Firebase classes
    -keep class com.google.firebase.** { *; }

    # Keep names for Kotlin serialization
    -keepclassmembernames class kotlinx.serialization.KSerializer { *; }

    # Keep enumerations
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }

    # Keep names for model classes
    -keepclassmembernames class com.henrisusanto.vitmate.data.model.** {
        *;
    }