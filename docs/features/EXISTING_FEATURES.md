# Amar Hisab — Existing Feature Contract Catalog

এই catalog-এ বর্তমানে বিদ্যমান গুরুত্বপূর্ণ feature-গুলোর intended behavior সংরক্ষিত। ভবিষ্যতে behavior পরিবর্তন হলে সংশ্লিষ্ট section update করতে হবে। নতুন feature হলে FEATURE_TEMPLATE অনুযায়ী আলাদা specification যোগ করতে হবে।

## Core Accounting
কাজ: Personal ও Home fund-এর প্রকৃত movement অনুযায়ী balance ও transaction semantics বজায় রাখা।
কীভাবে: income, expense, transfer, loan, repayment, lending এবং return-এর source অনুযায়ী হিসাব।
নিয়ম: ACCOUNTING_RULES বাধ্যতামূলক; historical transaction silently reinterpret করা যাবে না।

## Loan Management
কাজ: loan নেওয়া, repayment এবং remaining history track করা।
কীভাবে: loan entry → fund flow → repayment → updated balance/history।

## Lending ও Borrowing
কাজ: ব্যক্তিকে ধার দেওয়া/নেওয়া এবং return history।
কীভাবে: person + amount + date + note + due date; return হলে remaining due থেকে কমে।

## Person Photo
কাজ: ব্যক্তির জন্য একবার photo save করে পরবর্তী supported transaction-এ পুনঃব্যবহার।
কীভাবে: person identity অনুযায়ী saved photo reference ব্যবহার; missing photo হলে safe fallback।

## Payment Message
কাজ: payment/return-এর পরে total, paid এবং remaining তথ্যসহ shareable message তৈরি।
কীভাবে: actual transaction data থেকে message generate; message নিজে accounting mutation নয়।

## ১৫–১৫ Monthly Summary ও PDF
কাজ: প্রতি মাসের ১৫ তারিখ থেকে পরবর্তী ১৫ তারিখ পর্যন্ত income, expense, net cash flow ও transaction list দেখানো এবং PDF export।
কীভাবে: current cycle → filter → summary → PDF। Screen ও PDF একই period/data ব্যবহার করবে।

## Recap Notifications
কাজ: daily reminder, weekly recap এবং monthly recap scheduling।
কীভাবে: scheduler → notification → tap করলে সংশ্লিষ্ট app destination। Monthly recap ১৫–১৫ cycle-এর সঙ্গে aligned।

## Financial Calendar
কাজ: date-based financial activity দেখা।
কীভাবে: month navigation → date selection → selected date-এর transaction detail।

## Household Security
কাজ: shared Home data শুধু authorized household member-দের জন্য accessible রাখা।
কীভাবে: memberUids membership → Firestore authorization rules। পুরোনো household-এর migration প্রয়োজন হতে পারে।

## Backup ও Restore
কাজ: transaction, loan, lending, returns, wallets, currencies ও related data দীর্ঘমেয়াদে preserve/restore।
কীভাবে: versioned backup → validation → migration → restore।

## Anomaly Alert
কাজ: defined abnormal transaction pattern detect করে alert দেওয়া।
কীভাবে: transaction history analysis → anomaly condition → alert; accounting data mutate নয়।

## Smart Reminder
কাজ: transaction behavior অনুযায়ী relevant reminder।
কীভাবে: transaction state → reminder candidate → duplicate-safe reminder।

## Smart Duty Roster
কাজ: current duty, OFF এবং roster end countdown দেখানো।
কীভাবে: roster dates/status → current state → next relevant state/countdown।

## Birthday
কাজ: Home Birthday card থেকে birthday date manage করা।
কীভাবে: Gift icon → existing date picker → save → card update। Existing card layout অকারণে বদলানো যাবে না।

## Vehicle/Fleet Maintenance
কাজ: vehicle maintenance records এবং upcoming service tracking।
কীভাবে: vehicle → maintenance entry → history/next service; expense integration হলে accounting rules অনুসরণ।

## Firebase App Distribution
কাজ: versioned release trusted testers-দের distribute করা।
কীভাবে: build → distribution → tester install → version verify; optional update URL supported।

## Global UI Rule
সব user-facing text বাংলা। Existing behavior অকারণে বদলানো যাবে না।

## Documentation Rule
উপরের feature-এর behavior বদলালে code change-এর সঙ্গে এই catalog বা সংশ্লিষ্ট dedicated feature file update করতে হবে। নতুন feature হলে FEATURE_TEMPLATE পূরণ করে নতুন file যোগ করতে হবে।
