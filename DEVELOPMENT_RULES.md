# Amar Hisab — Development Rules (LOCKED)

এই ফাইলটি Amar Hisab-এর পরিবর্তন করার আগে প্রথমে পড়তে হবে।
যে কোনো মানুষ, AI agent, coding assistant বা automation এই repository-তে code পরিবর্তন করলে এই নিয়মগুলো follow করবে।

## 1. ভাষা — বাংলা বাধ্যতামূলক
- App-এর user-facing সব লেখা বাংলায় হবে।
- Button, dialog, snackbar/toast, notification, empty state, validation, error, confirmation, PDF title/label এবং নতুন feature-এর UI text বাংলা হতে হবে।
- User-facing English রাখা যাবে না, যদি না সেটি brand/product name, technical identifier, currency/code, অথবা ব্যবহারকারী ইচ্ছাকৃতভাবে English চান।
- Existing Bangla UI অকারণে English করা যাবে না।
- Code identifier, class/function/file name English হতে পারে; এটি user-facing text নয়।

## 2. Change করার আগে Audit বাধ্যতামূলক
কোনো file পরিবর্তনের আগে:
1. বর্তমান implementation পড়তে হবে।
2. সংশ্লিষ্ট model, ViewModel, repository, utility এবং UI dependency খুঁজে দেখতে হবে।
3. একই feature-এর duplicate implementation আছে কি না দেখতে হবে।
4. Existing behavior কীভাবে কাজ করছে তা বুঝতে হবে।
5. প্রয়োজনের বাইরে refactor করা যাবে না।

Audit ছাড়া blind edit/replace/merge করা যাবে না।

## 3. Minimal Change Rule
- User যে পরিবর্তন চেয়েছেন, শুধু সেটুকুই পরিবর্তন করতে হবে।
- Existing UI, calculation, navigation বা feature অকারণে বদলানো যাবে না।
- নতুন feature-এর জন্য existing working logic duplicate করা যাবে না।
- compatibility file/overload যোগ করার আগে duplicate/conflict audit করতে হবে।

## 4. Accounting Safety
ACCOUNTING_RULES.md সর্বোচ্চ অগ্রাধিকার পাবে।
- Accounting semantics পরিবর্তন করা যাবে না।
- Personal/Home fund logic পরিবর্তনের আগে explicit accounting review দরকার।
- Historical transaction-এর meaning silently পরিবর্তন করা যাবে না।
- Currency conversion, loan, lending, repayment, transfer এবং balance calculation পরিবর্তনের আগে regression impact দেখতে হবে।

## 5. Data Safety
- Existing user data/backup format ভাঙা যাবে না।
- Migration প্রয়োজন হলে backward-compatible migration ব্যবহার করতে হবে।
- Firestore data structure পরিবর্তনের আগে old records-এর compatibility যাচাই করতে হবে।
- Security rules পরিবর্তনের আগে affected read/write paths audit করতে হবে।

## 6. Git Safety
- feature/my-journey এবং stable/main branch সরাসরি overwrite করা যাবে না।
- আগে feature/fix/integration branch ব্যবহার করতে হবে।
- Force checkout, reset --hard, branch delete, branch rename বা destructive merge explicit approval ছাড়া করা যাবে না।
- Uncommitted local changes থাকলে আগে নিরাপদে preserve করতে হবে।
- Merge করার আগে compare + audit করতে হবে।

## 7. Build/Test Gate
কোনো change complete বলা যাবে না যতক্ষণ না:
- Gradle Sync সফল;
- Build/Rebuild সফল;
- সংশ্লিষ্ট feature runtime test করা হয়েছে;
- নতুন error হলে audit করে fix করা হয়েছে।

শুধু IDE-এর green check দেখে build successful বলা যাবে না।

## 8. UI/UX Rule
- Existing polished UI অকারণে বদলানো যাবে না।
- নতুন UI existing app-এর visual language অনুসরণ করবে।
- Text clipping, overlap, broken spacing, unusable buttons বা accidental English থাকতে পারবে না।
- PDF/notification UI-ও এই বাংলা rule-এর অন্তর্ভুক্ত।

## 9. Error Handling
- Error দেখলে প্রথমে root cause identify করতে হবে।
- Screenshot/IDE error দেখে blind code change করা যাবে না।
- একটি fix করার পর আবার build/test করতে হবে।
- Build না চালিয়ে “সব ঠিক” বলা যাবে না।

## 10. Final Checklist
- [ ] User-facing text বাংলা
- [ ] Existing feature preserved
- [ ] Duplicate implementation নেই
- [ ] Accounting rules preserved
- [ ] Data/security compatibility checked
- [ ] Build/Rebuild successful
- [ ] Runtime test completed
- [ ] Git branch/status audited
- [ ] Unrelated files changed হয়নি

Priority order:
Safety → Existing behavior → Accounting/Data rules → User request → UI polish → Refactoring

এই rule file-এর কোনো rule পরিবর্তন করতে হলে আগে explicit review/documentation করতে হবে.

## 11. Feature Documentation Gate
নতুন feature যোগ করলে FEATURE_RULES.md অবশ্যই follow করতে হবে। feature/* branch-এ app code change-এর সঙ্গে docs/features/ এর feature specification যোগ বা update করা বাধ্যতামূলক। Pull Request CI এই শর্ত যাচাই করবে।

Feature specification-এ কেন, কী করবে, কোথায়, কীভাবে, Data, accounting impact, existing relationship, validation, compatibility, test plan এবং acceptance criteria লিখতে হবে।
