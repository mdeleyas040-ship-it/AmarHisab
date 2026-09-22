# Amar Hisab Coding Rules

Before making any code change, read DEVELOPMENT_RULES.md and ACCOUNTING_RULES.md.

Mandatory:
- All user-facing app text must be Bangla.
- Audit before editing.
- Make minimal targeted changes.
- Preserve existing accounting, data, security and UI behavior.
- Do not create duplicate implementations.
- Do not perform destructive Git operations without explicit approval.
- Sync, Rebuild and runtime-test relevant changes before declaring success.

Do not bypass these rules for convenience.


## Feature Rule — বাধ্যতামূলক
Before adding or changing a feature, read FEATURE_RULES.md. A feature/* branch that changes app code must also add or update a feature specification under docs/features/. Do not merge a new feature without its specification. Include purpose, behavior, location, flow, data, accounting impact, dependencies, validation, compatibility, tests and acceptance criteria.
