# ایرتکس ۲.۰ — بازطراحی کامل رابط کاربری

نسخه ۲.۰ با رابط کاربری کاملاً جدید (Modern Minimal + Soft Glassmorphism) از صفر بازنویسی شده است.
منطق عملکردی و داده‌ی قانون (فصل‌ها، مواد، بندها، تبصره‌ها و موتور جستجو) بدون تغییر حفظ شده است.

## ساختار جدید

```
app/src/main/java/tax/app/mehdighobadi/
├── data/
│   ├── TaxData.kt            ← داده‌ی قانون (بدون تغییر)
│   └── AppRepository.kt      ← نشان‌گذاری‌ها، آمار مطالعه، پیشنهاد هوشمند (آفلاین)
├── theme/
│   └── ThemeManager.kt       ← حالت روشن/تاریک/سیستمی، رنگ اصلی، اندازه فونت، کاهش انیمیشن
└── ui/
    ├── main/MainActivity.kt      ← میزبان + نوار پایین شناور شیشه‌ای (۵ تب)
    ├── home/HomeFragment.kt      ← هدر مینیمال، کارت Hero، آمار شیشه‌ای، پیشنهاد هوشمند
    ├── bank/BankFragment.kt      ← بانک سوالات: فصل‌ها + جستجوی زنده
    ├── favorites/FavoritesFragment.kt
    ├── stats/StatsFragment.kt    ← شمارنده‌های متحرک + پیشرفت هر فصل
    ├── settings/SettingsFragment.kt
    ├── reader/ReaderActivity.kt  ← کارت سوال + نوار پیشرفت + قبلی/بعدی + نشان‌گذاری
    ├── reader/ArticlePageFragment.kt
    └── common/ViewExtensions.kt  ← انیمیشن stagger، press-scale، شمارنده متحرک، ارقام فارسی
```

## امکانات

- **فونت وزیرمتن** به‌صورت محلی در `res/font` (بدون CDN)
- **حالت روشن و تاریک** با پالت نیلی + دو رنگ جایگزین (فیروزه‌ای، بنفش)
- **نوار پایین شناور** با پس‌زمینه شیشه‌ای و نشانگر pill متحرک
- **صفحه سوالات** با کارت گرد، نوار پیشرفت، ترنزیشن slide جهت‌دار RTL و انیمیشن‌های stagger
- **حس هوشمند**: پیشنهاد «کدام فصل را تمرین کن» بر اساس روند مطالعه + شمارنده‌های متحرک
- **کاملاً آفلاین**: هیچ دسترسی شبکه‌ای در manifest وجود ندارد
- **دسترس‌پذیری**: هدف لمسی ≥ ۴۸dp، contentDescription روی آیکون‌ها، سوییچ کاهش انیمیشن

## ساخت

```bash
./gradlew :app:assembleDebug
```

- minSdk 24 — targetSdk 34 — Kotlin + ViewBinding — Material 3
