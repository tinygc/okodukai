# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Google API Client / Drive model classes rely on reflection and @Key annotations.
# Keep signatures + annotations and preserve model members to avoid
# "key error" / abstract-class instantiation failures in minified release builds.
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

-keep class com.google.api.client.util.Key
-keep class com.google.api.client.util.GenericData { *; }
-keep class com.google.api.client.json.GenericJson { *; }

-keepclassmembers class * {
	@com.google.api.client.util.Key <fields>;
}

-keep class com.google.api.services.drive.model.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.googleapis.**
-dontwarn com.google.api.client.http.**
-dontwarn com.google.api.services.drive.**

# Backup model classes: Gson uses Java reflection to serialize/deserialize these.
# Without keep rules, R8 renames the fields and the JSON keys become obfuscated
# (e.g., "backupSchemaVersion" -> "a"), causing import failures.
-keep class com.tinygc.okodukai.data.backup.BackupDocument { *; }
-keep class com.tinygc.okodukai.data.backup.BackupPayload { *; }
-keep class com.tinygc.okodukai.data.backup.BackupSettings { *; }

# Room entities used in backup payload: same reason — preserve field names for Gson.
-keep class com.tinygc.okodukai.data.local.entity.** { *; }
