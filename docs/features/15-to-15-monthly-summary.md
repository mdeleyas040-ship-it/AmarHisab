# ১৫–১৫ মাসিক সারসংক্ষেপ ও PDF

## 1. কেন
প্রতি মাসের ১৫ তারিখ থেকে পরের ১৫ তারিখ পর্যন্ত হিসাবকে একটি নির্দিষ্ট চক্রে দেখা ও সংরক্ষণ করার জন্য এই feature রাখা হয়েছে। PDF-টি ব্যবহারকারীর জন্য পড়তে সহজ এবং অ্যাপের ভাষা-নীতির সঙ্গে সামঞ্জস্যপূর্ণ হতে হবে।

## 2. কী করবে
- ১৫–১৫ হিসাবের সময়কাল দেখাবে।
- মোট আয়, মোট খরচ এবং নিট নগদ প্রবাহ দেখাবে।
- বিভাগ অনুযায়ী খরচ দেখাবে।
- সংশ্লিষ্ট লেনদেনের তালিকা PDF-এ দেবে।
- PDF-এর সব user-facing static text বাংলায় থাকবে।
- PDF সংরক্ষণের পর সম্ভব হলে PDF viewer-এ খুলবে।

## 3. কোথায়
- মাসিক সারসংক্ষেপ screen।
- মাসিক recap notification থেকে screen খোলা যায়।
- মাসিক সারসংক্ষেপ screen-এর PDF action থেকে PDF তৈরি হয়।
- PDF generation: `ReportExporter.kt`।

## 4. কীভাবে কাজ করবে
1. `MonthlySummaryUtils.currentCycle()` বর্তমান ১৫–১৫ cycle নির্ধারণ করে।
2. `forPeriod()` সেই সময়সীমার transaction filter করে।
3. Screen-এ summary দেখানো হয়।
4. PDF export-এ একই `MonthlySummary` থেকে income/expense/category/transaction data নেওয়া হয়।
5. Currency conversion-এর বিদ্যমান USD/BDT/MVR logic ব্যবহার করে PDF amount তৈরি হয়।
6. PDF-এর static label ও period display বাংলায় render করা হয়।
7. PDF file save হওয়ার পর installed PDF viewer খোলার চেষ্টা করা হয়।

## 5. Data
- Source: বিদ্যমান `Transaction` এবং `MonthlySummary`।
- কোনো নতুন transaction field যোগ করা হয় না।
- কোনো historical data পরিবর্তন করা হয় না।

## 6. Accounting impact
- নতুন কোনো accounting rule তৈরি করে না।
- আয়, খরচ ও net cash flow বিদ্যমান summary data থেকে গণনা করে।
- Personal/Home fund semantics পরিবর্তন করা যাবে না।
- PDF export কোনো transaction write বা accounting mutation করে না।

## 7. Existing feature relationship
- Recap Notification-এর মাসিক ১৫–১৫ flow-এর সঙ্গে যুক্ত।
- Monthly Summary screen-এর সঙ্গে যুক্ত।
- বিদ্যমান PDF exporter-এর export path ব্যবহার করে।
- Existing transaction ও currency conversion logic পুনরায় ব্যবহার করে; duplicate accounting engine তৈরি করে না।

## 8. Validation ও Error
- Empty transaction period হলে PDF-এ বাংলায় জানানো হবে যে কোনো লেনদেন পাওয়া যায়নি।
- PDF write failure হলে বাংলায় error দেখাবে।
- PDF viewer না খুললেও saved PDF নষ্ট হবে না; সংরক্ষণের ফলাফল জানানো হবে।

## 9. Backward compatibility
- Existing transaction model অপরিবর্তিত।
- Existing PDF export-এর accounting data source অপরিবর্তিত।
- PDF text/label পরিবর্তন presentation-only।
- কোনো migration প্রয়োজন নেই।

## 10. Test plan
- Gradle Sync।
- Rebuild/compile।
- Monthly Summary screen-এ ১৫–১৫ period যাচাই।
- PDF export।
- PDF first page-এ overlap যাচাই।
- PDF title, period, summary labels, category heading এবং transaction headings বাংলায় যাচাই।
- PDF viewer auto-open যাচাই।
- Existing main financial PDF export regression যাচাই।

## 11. Acceptance criteria
- [ ] ১৫–১৫ period সঠিক।
- [ ] মোট আয়/খরচ/net cash flow সঠিক।
- [ ] PDF layout-এ overlap নেই।
- [ ] PDF-এর static user-facing text বাংলায়।
- [ ] বাংলা month name সঠিক।
- [ ] transaction type-এর পরিচিত ধরন বাংলায়।
- [ ] PDF save সফল।
- [ ] viewer auto-open চেষ্টা সফল হলে PDF খুলবে।
- [ ] accounting logic অপরিবর্তিত।

## 12. Change history
- 2026-09-22: ১৫–১৫ monthly summary PDF-এর static labels ও period display বাংলায় করা হয়েছে; existing PDF layout fix সংরক্ষণ করা হয়েছে।
