# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\aessh\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.squareup.okhttp.**

-keep class com.mobsandgeeks.saripaar.** {*;}
-keep @com.mobsandgeeks.saripaar.annotation.ValidateUsing class * {*;}

-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**



#Gson (from Hawk library)
-keep class com.google.gson.** { *; }
-keepattributes Signature

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep class .R
-keep class **.R$* {
    <fields>;
}
-dontwarn okio.**

-keep class com.daimajia.easing.** { *; }
-keep interface com.daimajia.easing.** { *; }

#-dontwarn com.squareup.haha.guava.**
#-dontwarn com.squareup.haha.perflib.**
#-dontwarn com.squareup.haha.trove.**
-dontwarn com.squareup.leakcanary.**
#-keep class com.squareup.haha.** { *; }
-keep class com.squareup.leakcanary.** { *; }

#Manually (these are just warnings)
-dontwarn microsoft.aspnet.signalr.**
-dontwarn com.rey.material.**
-dontwarn com.orhanobut.**
-dontwarn com.marshalchen.ultimaterecyclerview.**
-dontwarn com.android.volley.toolbox.**

-dontwarn butterknife.internal.**

-keep class **$$ViewInjector { *; }

-keepnames class * { @butterknife.InjectView *;}

-dontwarn butterknife.Views$InjectViewProcessor

-dontwarn com.gc.materialdesign.views.**