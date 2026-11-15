# irtax | راهنمای ساخت APK

## مشخصات
- Package/Application ID: `tax.app.mehdighobadi`
- Kotlin + Android Views + View Binding
- AGP: `8.2.0`
- Gradle: `8.4`
- Kotlin Gradle Plugin: `1.9.20`
- JDK: `17`
- Compile/Target SDK: `33`
- Min SDK: `24`

## ساختار
سورس‌ها در مسیر استاندارد `app/src/main` قرار گرفته‌اند. فایل `local.properties` عمداً داخل ZIP نیست، چون مسیر SDK در هر کامپیوتر متفاوت است.

## تنظیم SDK در ویندوز
در ریشه پروژه، کنار `settings.gradle.kts` فایل `local.properties` بساز:

```properties
sdk.dir=C:/Users/USERNAME/AppData/Local/Android/Sdk
```

مسیر بالا را با مسیر واقعی SDK عوض کن. حداقل این بسته‌ها لازم‌اند:
- Android SDK Platform 33 (`platforms;android-33`)
- Android SDK Build-Tools 33.0.2 (`build-tools;33.0.2`)
- Android SDK Platform-Tools (`platform-tools`)

## تنظیم JDK
در Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle، گزینه Gradle JDK را روی JDK 17 قرار بده.

## Build با Gradle نصب‌شده
از CMD در ریشه پروژه اجرا کن:

```bat
gradle --version
gradle --offline :app:assembleDebug
```

اگر تمام Pluginها و کتابخانه‌ها در Cache باشند، خروجی در این مسیر ساخته می‌شود:

```text
appuild\outputspk\debugpp-debug.apk
```

## Build با Wrapper
Wrapper استاندارد به `https://services.gradle.org/distributions/gradle-8.4-bin.zip` اشاره می‌کند. چون این آدرس ممکن است از شبکه شما در دسترس نباشد، اگر Gradle 8.4 از قبل نصب است از دستور `gradle --offline` استفاده کن. Wrapper بدون توزیع Gradle در Cache قادر به اجرای آفلاین نیست.

## وابستگی‌های مستقیم
- `androidx.core:core-ktx:1.12.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.11.0`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `androidx.cardview:cardview:1.0.0`

وابستگی‌های انتقالی این کتابخانه‌ها نیز باید در Gradle Cache موجود باشند. وجود mirror مایکت در فایل مخازن به معنی تأیید موجود بودن همه Artifactها در آن نیست؛ در صورت نبود Artifact، Gradle به مخازن رسمی بعدی رجوع می‌کند.

## نکات مهم محتوا
محتوای `TaxData.kt` در سورس ورودی عمدتاً مربوط به «قانون پایانه‌های فروشگاهی و سامانه مؤدیان» است، نه متن کامل «قانون مالیات‌های مستقیم». متن‌های حقوقی عمداً در این تعمیر فنی بازنویسی یا تکمیل نشده‌اند و قبل از انتشار باید با متن رسمی و آخرین اصلاحات تطبیق داده شوند.

## محدودیت اعتبارسنجی این بسته
در محیط آماده‌سازی این ZIP، Android SDK و Gradle اجرایی نصب نبودند؛ بنابراین Build واقعی APK و تست روی شبیه‌ساز/دستگاه انجام نشده است. بررسی XML و کامپایل مستقل فایل داده Kotlin انجام شده است. اولین Build روی کامپیوتر توسعه‌دهنده همچنان لازم است.
