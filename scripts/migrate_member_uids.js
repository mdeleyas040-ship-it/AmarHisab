// scripts/migrate_member_uids.js
//
// পুরনো household document-গুলোতে memberUids field নেই (শুধু members array আছে)。
// নতুন firestore.rules memberUids দিয়ে member চেক করে, তাই এই script-টা
// প্রতিটা household document-এ members[].uid থেকে memberUids array বসিয়ে দেবে।
//
// চালানোর আগে: Firebase project-এর একটা Service Account JSON key লাগবে।
// এটি কখনো GitHub-এ commit করবেন না।
//
// চালানোর নিয়ম:
//   npm install firebase-admin
//   node scripts/migrate_member_uids.js ./serviceAccountKey.json

const admin = require("firebase-admin");

const keyPath = process.argv[2];
if (!keyPath) {
  console.error("Usage: node migrate_member_uids.js <path-to-service-account.json>");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(require("path").resolve(keyPath))),
});

const db = admin.firestore();

async function migrate() {
  const snapshot = await db.collection("households").get();
  console.log(`মোট ${snapshot.size} টা household পাওয়া গেছে।`);

  let updated = 0;
  for (const doc of snapshot.docs) {
    const data = doc.data();
    if (Array.isArray(data.memberUids)) {
      continue;
    }

    const members = Array.isArray(data.members) ? data.members : [];
    const memberUids = members.map((m) => m.uid).filter(Boolean);

    await doc.ref.update({ memberUids });
    console.log(`✔ ${doc.id} → memberUids: [${memberUids.join(", ")}]`);
    updated++;
  }

  console.log(`\nমোট ${updated} টা household আপডেট হয়েছে।`);
  process.exit(0);
}

migrate().catch((err) => {
  console.error("মাইগ্রেশন ব্যর্থ:", err);
  process.exit(1);
});
