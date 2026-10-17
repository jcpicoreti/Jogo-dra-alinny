# Project ProGuard / R8 Rules for Release Builds

# Room Database Keep Rules
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }

# Game Data Models & Moshi JSON Keep Rules
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Preserve Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve ViewModels
-keep class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Preserve Annotations & Reflection Attributes for Room/Moshi
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
