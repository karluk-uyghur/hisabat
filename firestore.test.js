const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validAccountPayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    name: "ئەخمەت",
    accountName: "ACC-1001",
    password: "secret",
    flowAmount: "100M / 1000GB",
    openDate: "2026-01-15",
    streetName: "ئازادلىق كوچىسى",
    houseNumber: "12-نومۇر",
    notes: "ئاكتىپ ھېسابات",
    months: Array(12).fill(false),
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or list accounts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("accounts").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("accounts")
      .doc("acc_1")
      .get()
  );
});

test("Unauthenticated user cannot create account", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("accounts")
      .doc("acc_1")
      .set(validAccountPayload(ALICE_UID))
  );
});

test("Authenticated owner can create, read, list, update, toggle months, and delete own account", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("accounts")
    .doc("acc_1");

  await assertSucceeds(docRef.set(validAccountPayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("accounts").get()
  );

  const updatedMonths = Array(12).fill(false);
  updatedMonths[0] = true;
  await assertSucceeds(
    docRef.update({
      months: updatedMonths,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    docRef.update({
      name: "مەمەت",
      notes: "يېڭىلاندى",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Authenticated user cannot read, list, create, update, or delete another user's accounts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("accounts")
      .doc("bob_acc")
      .set(validAccountPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobAccRef = aliceDb
    .collection("users")
    .doc(BOB_UID)
    .collection("accounts")
    .doc("bob_acc");

  await assertFails(bobAccRef.get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("accounts").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("accounts")
      .doc("alice_hack")
      .set(validAccountPayload(ALICE_UID))
  );
  await assertFails(bobAccRef.update({ name: "Hacked" }));
  await assertFails(bobAccRef.delete());
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("accounts")
    .doc("acc_shadow");

  await assertFails(
    docRef.set(validAccountPayload(ALICE_UID, { isAdmin: true }))
  );

  await assertSucceeds(docRef.set(validAccountPayload(ALICE_UID)));
  await assertFails(
    docRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Identity spoofing and immutable createdAt tampering are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("accounts")
    .doc("acc_immut");

  await assertFails(
    docRef.set(validAccountPayload(BOB_UID))
  );

  await assertSucceeds(docRef.set(validAccountPayload(ALICE_UID)));

  await assertFails(
    docRef.update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    docRef.update({
      createdAt: new Date(Date.now() - 100000),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Invalid schema values (empty name, wrong months length, oversized ID) are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const colRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("accounts");

  await assertFails(
    colRef.doc("bad_empty").set(validAccountPayload(ALICE_UID, { name: "" }))
  );
  await assertFails(
    colRef
      .doc("bad_months")
      .set(validAccountPayload(ALICE_UID, { months: [true, false] }))
  );
  await assertFails(
    colRef
      .doc("a".repeat(130))
      .set(validAccountPayload(ALICE_UID))
  );
});
