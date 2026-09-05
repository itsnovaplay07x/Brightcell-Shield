# Brightcell Shield V11
# Custom ProGuard / R8 rules

# Keep native Android antivirus engine classes
-keep class com.brightcell.shield.** { *; }

# Keep data classes used by scanner engines
-keepattributes *Annotation*

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
