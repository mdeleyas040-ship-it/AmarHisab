# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\eleya\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Firebase specific rules (if needed, usually handled by SDK)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep your data classes (Models) to avoid issues with JSON serialization
-keep class com.eleyas.expensetracker.model.Transaction { *; }
-keep class com.eleyas.expensetracker.LoanAccount { *; }
-keep class com.eleyas.expensetracker.LoanBorrowing { *; }
-keep class com.eleyas.expensetracker.LoanPayment { *; }
-keep class com.eleyas.expensetracker.LendingAccount { *; }
-keep class com.eleyas.expensetracker.LendingReturn { *; }
-keep class com.eleyas.expensetracker.CategoryBudget { *; }
-keep class com.eleyas.expensetracker.BackupData { *; }
-keep class com.eleyas.expensetracker.NotificationItem { *; }
