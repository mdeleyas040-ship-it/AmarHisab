# Fix Syntax Errors in MainActivity.kt

The user made several changes that introduced syntax errors, primarily misplaced or missing closing braces, which caused a large number of "Unresolved reference" errors because the `AmarHisabApp` composable was closed prematurely.

## User Review Required

> [!IMPORTANT]
> The `AmarHisabApp` function was closed too early (around line 1566), causing all subsequent code that depends on its local variables to fail. I will also fix a missing closing brace for an `if` block and a redundant closing brace at the end of the file.

## Proposed Changes

### MainActivity.kt fixes

#### [MODIFY] [MainActivity.kt](file:///C:/Users/eleya/AndroidStudioProjects/AmarHisab/app/src/main/java/com/eleyas/expensetracker/MainActivity.kt)
- Remove the misplaced `}` at line 1566 that incorrectly closes `AmarHisabApp`.
- Add the missing `}` to close the `if (showAddDialog)` block at line 1604.
- Remove the extra `}` at line 6401 (based on the user's diff) to ensure the scope is correct.

## Verification Plan

### Automated Tests
- Run `analyze_file` on `MainActivity.kt` to ensure build errors are resolved.
- Run `gradle_build` to verify the project compiles.
