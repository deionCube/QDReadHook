#---------------------------------基本指令区---------------------------------
# 指定代码的压缩级别
-optimizationpasses 7
-flattenpackagehierarchy
-allowaccessmodification
# 避免混淆Annotation、内部类、泛型、匿名类
-keepattributes Signature,Exceptions,*Annotation*,
                InnerClasses,PermittedSubclasses,EnclosingMethod,
                Deprecated,SourceFile,LineNumberTable
-keepattributes 'SourceFile'
-renamesourcefileattribute '希涵'
-obfuscationdictionary 'dictionary.txt'
-classobfuscationdictionary 'dictionary.txt'
-packageobfuscationdictionary 'dictionary.txt'
#混淆时不使用大小写混合，混淆后的类名为小写(大小写混淆容易导致class文件相互覆盖）
-dontusemixedcaseclassnames
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializersKt
-keep,includedescriptorclasses class cn.xihan.qdds.**$$serializer { *; }
-keepclassmembers class cn.xihan.qdds.** {
    *** Companion;
}
-keepclasseswithmembers class cn.xihan.qdds.** {
    kotlinx.serialization.KSerializer serializer(...);
}