# Maths Formula Memorizer Offline

A simple native Android app for memorizing maths formulas with a Duolingo-style flow:

1. Choose class
2. Choose unit
3. Learn one formula
4. Answer 3 quick questions
5. Repeat weak formulas
6. Track progress and streak locally

## What is already built

- Native Android Java app
- No server
- No login
- No ads
- No analytics
- No INTERNET permission in AndroidManifest.xml
- Local progress saved with SharedPreferences
- Class selection: Class 6, 7, 8, 9, 10, General Practice
- Learning path grouped by topic
- Formula lesson screen
- 3-question quiz screen
- Weak formulas screen
- Progress and streak screen
- App icon vector placeholder
- Formula database included in MainActivity.java

## How to open on Windows

1. Install Android Studio.
2. Open Android Studio.
3. Click **Open**.
4. Select this folder: `maths-formula-memorizer`.
5. Wait for Gradle sync.
6. Connect your Android phone with USB debugging enabled.
7. Press **Run**.

## How to change the app name

Edit:

`app/src/main/res/values/strings.xml`

```xml
<string name="app_name">Maths Formula Memorizer</string>
```

## How to change package name before Play Store

Current package/application id:

`com.mominhayat.mathsformulamemorizer`

For Play Store, keep it stable. Once uploaded, do not change package name.

Edit in:

`app/build.gradle`

```gradle
namespace 'com.mominhayat.mathsformulamemorizer'
applicationId 'com.mominhayat.mathsformulamemorizer'
```

Also move the Java file folder if you rename the package.

## How to add formulas

Open:

`app/src/main/java/com/mominhayat/mathsformulamemorizer/MainActivity.java`

Search for:

```java
private static class FormulaRepository
```

Add formulas using this pattern:

```java
f.add(new Formula(
    "unique_id_here",
    8,
    "Topic Name",
    "Formula Name",
    "Formula here",
    "Short explanation here.",
    "Short example here.",
    "Meaning question here?",
    new String[]{"Correct option", "Wrong option", "Wrong option", "Wrong option"},
    "Correct option"
));
```

## Build a signed AAB later

In Android Studio:

1. Build > Generate Signed Bundle / APK
2. Choose Android App Bundle
3. Create or choose signing key
4. Build release
5. Upload the `.aab` to Play Console

## Important before launch

- Review every formula for accuracy.
- Replace placeholder icon with a professional Play Store icon.
- Test on at least 2 Android phones.
- Prepare Play Store screenshots.
- For a new personal Google Play account, expect closed testing before production release.

## Physics app later

This app can become a template. For Physics:

1. Copy this project.
2. Change app name.
3. Change package name.
4. Replace formula database.
5. Change icon and screenshots.
