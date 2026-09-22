# Smart Duty Roster

## 1. কেন
Duty roster image থেকে নিজের duty, OFF day এবং roster cycle দ্রুত দেখা ও notification পাওয়ার জন্য।

## 2. কী করবে
- Roster image upload করে OCR scan করবে।
- Date-wise staff duty parse করবে।
- User নিজের নাম select করে save করবে।
- Daily duty notification, next OFF countdown এবং roster শেষ হওয়ার reminder দেখাবে।
- OFF-এর আগের preparation tasks সংরক্ষণ ও notification-এ দেখাবে।

## 3. কোথায়
- Top Menu → ডিউটি রোস্টার
- নতুন screen: `dutyroster/`
- Navigation wiring: `MainActivity.kt`
- Menu wiring: `TopMenu.kt`
- Notification receiver: `DutyRosterNotification.kt`

## 4. কীভাবে কাজ করবে
1. User roster image নির্বাচন করবে।
2. ML Kit text recognition OCR চালাবে।
3. `DutyRosterParser` date columns, names এবং duty cells parse করবে।
4. User নিজের নাম এবং reminder time confirm করবে।
5. `DutyRosterStorage` local SharedPreferences-এ roster রাখবে।
6. AlarmManager দিয়ে daily, OFF-eve এবং roster শেষ-day notification schedule হবে।

## 5. Data
Local data:
- uploadedAt
- sourceName
- myName
- notificationHour
- notificationMinute
- date-wise duty entries
- preparation tasks

কোনো accounting transaction তৈরি বা পরিবর্তন করে না।

## 6. Accounting impact
None. Duty Roster accounting engine, transaction balance, loan, lending বা wallet semantics পরিবর্তন করে না।

## 7. Existing feature relationship
বর্তমান Amar Hisab navigation ও notification architecture ব্যবহার করে। Existing accounting logic untouched থাকে। Main navigation থেকে screen open হয় এবং notification tap করলে Duty Roster screen খোলে।

## 8. Validation ও Error
- OCR image পড়তে না পারলে user-facing error দেখায়।
- Date/employee table পাওয়া না গেলে scan ব্যর্থ হিসেবে জানায়।
- Empty roster silently save করা হয় না।
- Notification schedule local roster না থাকলে skip করে।
- User name OCR দিয়ে অনুমান না করে manual selection ব্যবহার করে।

## 9. Backward compatibility
Existing transactions, loans, lending, wallets, backup data এবং accounting calculations unaffected। Duty Roster data আলাদা SharedPreferences namespace-এ রাখা হয়।

## 10. Test plan
- Gradle unit/regression tests pass করতে হবে।
- Accounting Lock CI pass করতে হবে।
- Feature Documentation Gate pass করতে হবে।
- Main branch build/compile pass করতে হবে।
- Roster screen navigation এবং notification intent manually verify করতে হবে।

## 11. Acceptance criteria
- Top Menu থেকে Duty Roster খুলবে।
- Image upload ও OCR parse flow কাজ করবে।
- নিজের নাম নির্বাচন ও roster save হবে।
- Today Duty এবং next OFF/roster countdown দেখাবে।
- Daily reminder schedule হবে।
- OFF-eve এবং roster শেষ-day notification কাজ করবে।
- Existing accounting behavior অপরিবর্তিত থাকবে।

## 12. Change history
- 2026-09-22: Audited legacy Duty Roster branch and ported the feature onto current `main` through a fresh feature branch.
