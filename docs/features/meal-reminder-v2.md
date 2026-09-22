# Meal Reminder v2

## 1. কেন
দৈনিক খাবারের সময় মনে করিয়ে দেওয়ার জন্য একটি lightweight local reminder রাখা।

## 2. কী করবে
সকাল 7:00, দুপুর 11:00 এবং রাত 6:00-এ খাবারের reminder notification দেবে। Notification চাপলে সংশ্লিষ্ট খাবারের Bangla তালিকা খুলবে।

## 3. কোথায়
- `meal/MealReminderManager.kt`
- `meal/MealReminderReceiver.kt`
- `meal/MealDetailsActivity.kt`
- `AmarHisabApplication.kt`
- `AndroidManifest.xml`

## 4. কীভাবে কাজ করবে
Application startup-এ notification channel তৈরি ও daily alarms schedule হবে। Boot/package replacement-এর পর receiver alarms পুনরায় schedule করবে। Reminder fire হলে notification দেখিয়ে পরের দিনের একই meal alarm schedule করবে।

## 5. Data
কোনো accounting transaction, loan, lending বা Firestore data পরিবর্তন করে না। Enabled state শুধু local SharedPreferences-এ থাকে।

## 6. Accounting impact
কোনো accounting impact নেই।

## 7. Existing feature relationship
বিদ্যমান financial reminder, monthly recap এবং Smart Duty Roster notification-এর পাশাপাশি আলাদা notification channel/action ব্যবহার করে। Accounting lock-এর কোনো file পরিবর্তন করে না।

## 8. Validation ও Error
Notification permission না থাকলে notification silently skipped হবে। Alarm scheduling-এর SecurityException fallback রাখা হয়েছে।

## 9. Backward compatibility
পুরোনো transaction/backup data অপরিবর্তিত থাকবে। Existing notification receivers অপরিবর্তিত থাকবে।

## 10. Test plan
- Debug build
- App startup-এ meal alarms schedule
- Boot/package replacement reschedule
- Breakfast/lunch/dinner notification payload
- Notification tap opens correct meal details
- Accounting regression tests

## 11. Acceptance criteria
- তিনটি meal reminder schedule হয়।
- Notification থেকে সঠিক meal details screen খোলে।
- Existing accounting tests/build ভাঙে না।

## 12. Change history
- 2026-09-22: Meal reminder feature ported from legacy branch onto current main baseline.
