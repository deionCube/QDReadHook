-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializersKt
-keep,includedescriptorclasses class cn.xihan.qdds.**$$serializer { *; }
-keepclassmembers class cn.xihan.qdds.** {
    *** Companion;
}
-keepclasseswithmembers class cn.xihan.qdds.** {
    kotlinx.serialization.KSerializer serializer(...);
}