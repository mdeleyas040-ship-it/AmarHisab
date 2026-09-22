# Amar Hisab

Amar Hisab is a Bangla-first personal and household expense tracker.

## Firestore household security

Household membership is represented by both the existing `members` array and a `memberUids` array. Firestore Security Rules use `memberUids` for authorization.

For old household documents, run:

```bash
npm install firebase-admin
node scripts/migrate_member_uids.js ./serviceAccountKey.json
```

Never commit a Firebase service-account key to this repository.

## Shared household data

Shared home transactions are stored under `households/{householdId}/homeTransactions`.

Only authenticated household members should be able to read or write shared household data.
