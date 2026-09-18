# Amar Hisab — Permanent Accounting Rules

> **LOCKED ACCOUNTING CONTRACT**
>
> These rules define the accounting model of Amar Hisab. They are part of the data contract, not UI behavior.
> Any future change that can alter these rules must be treated as a breaking accounting change and must not be merged without an explicit accounting review.

## 1. Core fund model

Amar Hisab has two logical funds:

- **Personal** — the user's personal money.
- **Home** — money belonging to the Home fund.

A transaction must affect only the fund from which the money actually moves.

## 2. Permanent rules

1. **Loan proceeds enter Personal first.**
2. **Personal → Home** is a transfer. It moves money from Personal to Home; it is not Home income.
3. **Home Expense** reduces Home only.
4. **Home Loan Repayment** reduces Home only.
5. **Home Lending** reduces Home only.
6. **Home Lending Return** increases Home only.
7. **Personal Loan Repayment** reduces Personal only.
8. **Personal Lending** reduces Personal only.
9. **Personal Lending Return** increases Personal only.
10. A Home-funded movement must never be subtracted from Personal as well.
11. A Personal-funded movement must never be counted as Home money.
12. Adding or editing UI fields must never silently change the fund semantics.
13. The loan popup's **source type** (for example, bank/person) is not the same concept as the accounting fund.
14. Existing transaction history must remain readable after future app versions.
15. Backup/restore must preserve transactions, loans, loan payments, lendings, returns, wallets, currencies and their fund-source information.

## 3. Canonical flow

**Loan → Personal → Home → Home Expense / Home Loan Repayment / Home Lending → Home Lending Return**

Personal-only flow:

**Personal → Personal Expense / Personal Loan Repayment / Personal Lending → Personal Lending Return**

## 4. Change-control rule

The accounting engine and models covered by the accounting lock are protected by CI.

If a future change modifies a locked accounting file, CI must fail until the lock is intentionally reviewed and updated.

A lock update is allowed only when:
- the accounting behavior has been explicitly reviewed;
- regression tests have been added/updated;
- old backup data remains compatible;
- the change is documented in the commit/PR.

## 5. Ten-year data principle

The goal is **not merely to preserve today's code**. The goal is to preserve the meaning of historical money records.

Future developers must prefer:
- additive migrations;
- backward-compatible fields;
- explicit versioning;
- backup/restore tests;
- deterministic accounting calculations.

Never silently reinterpret an old transaction.

**Locked baseline:** 2026-09-18  
**Protected branch baseline:** feature/my-journey
