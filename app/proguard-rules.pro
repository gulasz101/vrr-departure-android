# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK directory tools/proguard/proguard-android.txt

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.vrr.departureboard.**$$serializer { *; }
-keepclassmembers class com.vrr.departureboard.** {
    *** Companion;
}
-keepclasseswithmembers class com.vrr.departureboard.** {
    kotlinx.serialization.KSerializer serializer(...);
}
