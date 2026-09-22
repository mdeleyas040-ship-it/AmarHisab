# Amar Hisab — বাধ্যতামূলক Feature Documentation Rule

## মূল নিয়ম
নতুন feature code-এ যোগ করার আগে বা code-এর সঙ্গে তার Feature Rule/Specification file যোগ করা বাধ্যতামূলক।

প্রতিটি নতুন feature specification-এ অবশ্যই লিখতে হবে:
1. কেন — feature কেন দরকার।
2. কী করবে — user-facing behavior।
3. কোথায় — screen/menu/navigation location।
4. কীভাবে — step-by-step user ও internal flow।
5. Data — model/storage/Firestore/preferences।
6. Accounting impact — থাকলে ACCOUNTING_RULES অনুসরণ।
7. Existing feature relationship — কোন feature-এর সঙ্গে যুক্ত।
8. Validation ও Error behavior।
9. Notification/PDF/export impact।
10. Backward compatibility ও migration।
11. Test plan।
12. Acceptance criteria।

## বাধ্যতামূলক workflow
Audit → Feature Rule লিখুন → Code করুন → Build → Test → Documentation update → Review → Merge

Feature specification ছাড়া নতুন feature merge করা যাবে না।

## Existing feature পরিবর্তন
পুরোনো feature-এর behavior change হলেও সংশ্লিষ্ট specification update করতে হবে।

## Exception
শুধু typo, formatting, comment, dependency maintenance বা pure bug-fix হলে নতুন specification দরকার নেই। কিন্তু bug-fix যদি feature behavior বদলায়, specification update করতে হবে।

## Priority
ACCOUNTING_RULES → DEVELOPMENT_RULES → এই feature contract → user request → implementation convenience
