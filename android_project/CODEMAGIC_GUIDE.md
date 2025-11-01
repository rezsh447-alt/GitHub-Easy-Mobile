# راهنمای استفاده از CodeMagic برای ساخت GitHub Easy Mobile

## مرحله 1: آماده‌سازی پروژه

### فایل‌های مورد نیاز ایجاد شده:
- `codemagic.yaml` - فایل تنظیمات CI/CD
- `gradle/wrapper/gradle-wrapper.properties` - تنظیمات Gradle Wrapper
- `gradle/wrapper/gradle-wrapper.jar` - فایل JAR مورد نیاز
- `gradlew` - اسکریپت اجرای Gradle

### ساختار پروژه نهایی:
```
android_project/
├── app/
├── gradle/wrapper/
├── gradlew
├── build.gradle
├── settings.gradle
└── codemagic.yaml
```

## مرحله 2: راه‌اندازی CodeMagic

### 2.1 ایجاد حساب CodeMagic
1. به [codemagic.io](https://codemagic.io) بروید
2. با GitHub، GitLab یا Bitbucket وارد شوید
3. اتصال به مخزن GitHub خود

### 2.2 آپلود پروژه
```bash
# اگر هنوز کدها در GitHub نیستند:
git init
git add .
git commit -m "Initial commit - GitHub Easy Mobile"
git branch -M main
git remote add origin YOUR_GITHUB_REPO_URL
git push -u origin main
```

### 2.3 اتصال در CodeMagic
1. در داشبورد CodeMagic روی "Add application" کلیک کنید
2. مخزن GitHub خود را انتخاب کنید
3. تنظیمات پیش‌فرض را تایید کنید (CodeMagic به‌طور خودکار Android را شناسایی می‌کند)

## مرحله 3: تنظیمات Build

### 3.1 انتخاب Workflow
در تنظیمات پروژه، می‌توانید یکی از این گزینه‌ها را انتخاب کنید:

**Android Debug Build:**
- سریع‌تر (حدود 3-5 دقیقه)
- فایل APK برای تست
- نیازی به امضای دیجیتال ندارد

**Android Release Build:**
- کندتر (حدود 5-8 دقیقه)
- فایل‌های APK و AAB
- نیاز به keystore برای امضای دیجیتال

### 3.2 تنظیمات متغیرها (Environment Variables)

برای Release Build، این متغیرها را تنظیم کنید:

```
PACKAGE_NAME=com.github.easymobile
```

### 3.3 تنظیم Keystore برای Release
برای ساخت فایل Release باید keystore بسازید:

1. **ایجاد Keystore:**
```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

2. **آپلود در CodeMagic:**
- به بخش "Android signing" بروید
- فایل keystore را آپلود کنید
- رمز عبور keystore را وارد کنید

## مرحله 4: اجرای Build

### 4.1 شروع Build
1. روی "Start build" کلیک کنید
2. Workflow مورد نظر را انتخاب کنید
3. منتظر تکمیل بمانید

### 4.2 مشاهده Log‌ها
در حین build می‌توانید:
- مراحل مختلف را مشاهده کنید
- خطاها را بررسی کنید
- زمان باقی‌مانده را ببینید

### 4.3 دانلود APK
پس از موفقیت:
- APK در بخش "Artifacts" آماده دانلود است
- APK Debug: حدود 10-15 مگابایت
- APK Release: حدود 8-12 مگابایت

## مرحله 5: تست APK

### 5.1 نصب روی گوشی
```bash
# فعال‌سازی Developer Mode در گوشی Android
# فعال‌سازی USB Debugging
adb install app-debug.apk
```

### 5.2 تنظیم GitHub Token
1. اپلیکیشن را باز کنید
2. به Settings بروید
3. GitHub Personal Access Token خود را وارد کنید

### 5.3 ایجاد GitHub Token
1. در GitHub به Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Scope‌های مورد نیاز:
   - `repo` (دسترسی کامل به ریپازیتوری‌ها)
   - `user` (دسترسی به اطلاعات کاربر)
4. Token را کپی کرده و در اپلیکیشن وارد کنید

## مرحله 6: اتوماسیون Build

### 6.1 Build خودکار با Push
در CodeMagic می‌توانید تنظیم کنید که:
- با هر push در master/main اتوماتیک build شود
- با هر pull request تست شود
- در زمان‌بندی مشخص build شود

### 6.2 Build Conditions
```yaml
# نمونه شرط build
trigger:
  events:
    - push
    - pull_request
  branch_patterns:
    - pattern: 'main'
      source: true
    - pattern: 'release/*'
      source: true
```

## عیب‌یابی مشکلات رایج

### خطای Build Fail
- **Gradle Sync Error:** مطمئن شوید Gradle Wrapper درست است
- **SDK Version Error:** به AndroidManifest.xml مراجعه کنید
- **Memory Error:** instance_type را به android_large تغییر دهید

### مشکل Keystore
- فایل keystore آسیب دیده: دوباره بسازید
- رمز عبور اشتباه: از کلمه عبور درست استفاده کنید

### مشکل Dependency
- Internet Connection: پایداری اتصال را بررسی کنید
- Version Conflict: versions در build.gradle را چک کنید

## مزایای استفاده از CodeMagic

✅ **ساخت سریع:** Build در کمتر از 5 دقیقه
✅ **رایگان:** برای مخازن عمومی رایگان کامل
✅ **آپلود مستقیم:** ارسال به Play Store مستقیم
✅ **تست خودکار:** تست در دستگاه‌های مختلف
✅ **Log کامل:** بررسی دقیق خطاها

## پشتیبانی و راهنمایی

اگر مشکلی داشتید:
1. Log‌های build را بررسی کنید
2. فایل codemagic.yaml را چک کنید
3. مستندات رسمی CodeMagic را مطالعه کنید

**هدف نهایی:** ساخت APK آماده نصب بر روی گوشی شما با قابلیت‌های کامل GitHub management!