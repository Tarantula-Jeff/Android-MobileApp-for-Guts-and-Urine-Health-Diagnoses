# Doctor workflow setup

The app now writes patient profiles to `users`, in-app urgent alerts to `alerts`, and stool/urine sample history records to `sampleHistory` in Cloud Firestore. Images selected in alert preferences and all analyzed stool/urine samples are uploaded to Firebase Storage.

1. In the Firebase Console, enable **Cloud Firestore** and **Storage** for the project in `app/google-services.json`.
2. Deploy the supplied `firestore.rules`. These rules prevent patients from assigning themselves the doctor role and only expose patient data to their linked doctor.
3. Create a Firebase Authentication account for each doctor. In Firestore, create `users/<doctor Firebase UID>` with `email: "doctor@example.com"` and `role: "doctor"`.
4. The doctor signs into this same app and is automatically routed to the Doctor Dashboard. Their app ID is displayed at the top of the dashboard.
5. The patient enters that app ID under **Alert & sharing settings**. This assigns the patient to the doctor and enables secure in-app urgent alerts; they may instead select SMS as a fallback.

For real background push notifications, add Firebase Cloud Messaging and a trusted server or Cloud Function that listens for newly created `alerts` documents. Do not send FCM messages directly from the Android client because that would expose server credentials.

Before handling real clinical data, obtain appropriate legal, privacy, consent, retention, and security review for your jurisdiction. This project is a technical starting point, not a production medical-record system.
