# Person Profile & Photo Reuse

## 1. কেন
একই ব্যক্তির নামে বারবার লেনদেন যোগ করার সময় একই তথ্য ও ছবি পুনরায় দিতে না হয় এবং ব্যক্তিকে সহজে শনাক্ত করা যায়।

## 2. কী করবে
- নতুন ব্যক্তির profile তৈরি করা যাবে।
- ব্যক্তির ছবি যোগ করা যাবে।
- আগে যোগ করা ব্যক্তিকে transaction dialog থেকে নির্বাচন করা যাবে।
- নির্বাচিত profile-এর stable ID loan/lending-এর সঙ্গে সংরক্ষিত থাকবে।
- পুরোনো transaction-এর জন্য personId না থাকলেও transaction চলবে।

## 3. কোথায়
- Loan/Lending data model
- Local account storage
- Lending entry dialog
- Firebase loan/lending sync metadata
- Person profile photo local storage

## 4. কীভাবে কাজ করবে
ব্যক্তির নাম দিয়ে existing profile খোঁজা হবে। নতুন ব্যক্তি হলে একটি stable profile ID তৈরি হবে। ছবি app-এর internal person_profiles directory-তে copy হবে। Transaction-এ ব্যক্তির নামের পাশাপাশি optional personId রাখা হবে, যাতে পরবর্তী transaction একই profile-এর সঙ্গে যুক্ত হতে পারে।

## 5. Data
PersonProfile-এ ID, name, photoUri, phone, note, createdAt এবং updatedAt রাখা হয়। Loan ও Lending-এ nullable personId রাখা হয়েছে। Existing records-এর personId null থাকতে পারে।

## 6. Accounting impact
কোনো accounting formula, balance calculation, loan/lending amount calculation, return calculation বা ledger calculation পরিবর্তন করা হয়নি। personId শুধুমাত্র identity/profile metadata।

## 7. Existing feature relationship
Loan, Lending, local backup/storage এবং Firebase sync-এর existing functionality বজায় থাকবে। Existing transaction-এর name/person field অপরিবর্তিত থাকবে।

## 8. Validation ও Error
- খালি নাম থাকলে photo profile তৈরি করা হবে না।
- Photo copy ব্যর্থ হলে transaction-এর accounting data পরিবর্তন হবে না।
- Profile photo delete helper শুধুমাত্র app-এর নিজস্ব profile-photo directory-এর file delete করতে পারবে।
- Missing/legacy personId হলে existing transaction fallback হিসেবে name/person field ব্যবহার করবে।

## 9. Backward compatibility
personId nullable হওয়ায় পুরোনো Loan/Lending data পড়তে কোনো নতুন field বাধ্যতামূলক নয়। পুরোনো backup data-এর জন্য missing personId null হিসেবে থাকবে।

## 10. Test plan
- Existing accounting regression tests চালাতে হবে।
- Loan/Lending-এর legacy data compatibility যাচাই করতে হবে।
- Person profile local persistence round-trip যাচাই করতে হবে।
- personId persistence যাচাই করতে হবে।
- একই ব্যক্তিকে পুনরায় নির্বাচন করলে একই profile ID ব্যবহৃত হচ্ছে কিনা যাচাই করতে হবে।
- CI Accounting Lock ও Feature Documentation Gate PASS করতে হবে।

## 11. Acceptance criteria
- নতুন ব্যক্তি ও ছবি সংরক্ষণ করা যায়।
- আগের ব্যক্তি নির্বাচন করা যায়।
- নতুন transaction-এ profile ID সংযুক্ত হয়।
- accounting totals/formulas অপরিবর্তিত থাকে।
- legacy transaction কাজ করে।
- required CI checks PASS না হওয়া পর্যন্ত feature merge করা হবে না।

## 12. Change history
- 2026-09-22: PersonProfile model, local profile/photo storage এবং Loan/Lending personId ভিত্তি যোগ করা হয়েছে।
- 2026-09-22: Lending dialog-এ existing person selection ও photo entry যোগ করা হয়েছে।
- 2026-09-22: Loan dialog-এ reusable person selection/photo entry, explicit personId linkage এবং Firestore restore যোগ করা হয়েছে।
- 2026-09-22: Backup export/import-এ person profile metadata এবং loan/lending personId preservation যোগ করা হয়েছে।
- 2026-09-22: CI audit অনুযায়ী feature documentation এবং accounting-lock baseline update করা হচ্ছে।
