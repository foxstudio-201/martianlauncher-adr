-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.github.luben.zstd.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Launcher
-keep class org.lwjgl.glfw.CallbackBridge {
    *;
}
-keep class com.oracle.dalvik.VMLauncher {
    *;
}

#
## Hilt
#-keep class dagger.hilt.** { *; }
#-keep class javax.inject.** { *; }
#-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
#-keepclasseswithmembers class * {
#    @dagger.hilt.* <methods>;
#}

# Prevent R8 from over-optimizing constructors (causes StackOverflow with Hilt + proguard-android-optimize.txt)
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.internal.GeneratedEntryPoint <init>(...);
}
-keep,allowobfuscation @dagger.hilt.android.AndroidEntryPoint class *


-keep class com.foxstudio.martianlauncher.bridge.** { *; }
-keep class com.foxstudio.martianlauncher.utils.device.VulkanChecker {
    *;
}
-keep class com.foxstudio.martianlauncher.utils.device.VulkanCapabilities {
    *;
}
-keep interface com.foxstudio.martianlauncher.utils.device.VulkanLogCallback {
    *;
}
-keep class com.foxstudio.martianlauncher.game.input.CriticalNativeTest {
    *;
}

# Libraries
-keep class com.github.steveice10.opennbt.** { *; }