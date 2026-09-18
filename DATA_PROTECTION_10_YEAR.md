# Amar Hisab — 10-Year Data Protection Contract

**Baseline:** 2026-09-18
**Current backup schema:** version 4

## Purpose
Historical financial records are accounting data. Future app updates must not silently change the meaning of old records.

## Protected data
Backups must preserve transactions, loans, loan payments, lendings, lending returns, wallets/accounts, currencies, recorded exchange rates, fund-source information, linked IDs, dates, notes and edit history where present.

## Compatibility rules
1. Existing backup files must remain importable by future versions.
2. New fields should be additive whenever possible.
3. A required schema change must increment `backupVersion` and include a migration path for older versions.
4. Never silently reinterpret an old persisted field.
5. Never delete or rename a persisted accounting field without migration.
6. Missing fields in old backups must receive safe historical defaults.
7. Restore must preserve IDs so linked records remain linked.
8. Historical exchange rates stored with transactions must not be replaced by today's live rate.
9. Personal/Home fund semantics are governed by `ACCOUNTING_RULES.md`.
10. Manual and auto backup must preserve the same canonical accounting data.

## Recovery principle
The source of truth is preserved financial history, not a displayed balance. Balances should remain recalculable from records.

## Release rule
Changes to persistence, serialization, migration, accounting models or accounting engines require compatibility review, regression tests, migration documentation when needed, and successful Accounting Lock CI.

## Long-term archive
For records intended to survive 10+ years, keep periodic exported JSON backups outside the phone in at least two independent storage locations. Code protection cannot protect against lost hardware, deleted accounts or external service failure.

This contract improves long-term safety but cannot guarantee preservation against every future hardware, account or service failure.
