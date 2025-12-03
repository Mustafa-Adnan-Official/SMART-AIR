# SMART AIR — README.md

---

# SMART AIR

### *A Kid-Friendly Asthma Management App for Children, Parents, and Healthcare Providers*

Fall 2025 — CSCB07 Software Design Project
University of Toronto Scarborough

---

## 1. Overview

SMART AIR is an Android application designed to help children (ages 6–16) manage asthma more effectively while keeping parents and healthcare providers informed. The app enables:

* Logging rescue/controller doses
* Tracking symptoms, triggers, and peak flow
* Monitoring inhaler technique
* Providing simple streaks/badges for motivation
* Generating secure provider-ready reports
* Parent-controlled sharing of child data

The app was developed using **Android Studio (Java)**, **Firebase Authentication**, and **Firestore** following the project's required user stories and Scrum methodology.

---

## 2. Key Features (R1–R6)

### R1 — Accounts, Roles & Onboarding

* Email/password login for Parent & Provider
* Parent-linked Child profiles (no email needed)
* Role-based routing (Child / Parent / Provider)
* First-time onboarding with asthma basics & privacy settings
* Secure sign-in / sign-out flow
* Blocked access to protected screens when logged out

### R2 — Parent–Child Linking & Selective Sharing

* Parent can link/manage multiple children
* Provider invitation code system (one-time, 7 days)
* Per-child sharing toggles for data types:

  * Rescue logs
  * Controller adherence summary
  * Symptoms
  * Triggers
  * Peak Flow
  * Triage Incident Summary
  * Trend Charts
* Real-time permission updates

### R3 — Medicines, Technique & Motivation

* Separate *Rescue* and *Controller* logging
* Dose count + before/after feeling
* Technique Trainer: step-by-step guidance
* Inventory tracking (expiry, doses left, alerts)
* Motivation system: streaks + badges
* Achievements stored in Firestore per child

### R4 — Safety & Triage (One-Tap Emergency Helper)

* In-app triage flow:

  * Red flag detection
  * Home steps for Yellow/Green zone
  * Escalation logic
* Parent emergency alerts
* Incident logging stored in Firestore

### R5 — Symptoms, Triggers & History

* Daily mood and symptom check-in
* Trigger tagging (dust, exercise, illness, etc.)
* Peak flow entries
* History browser with 3–6 month filter
* Exportable PDF history (for provider use)

### R6 — Parent Dashboard & Provider Report

* Parent dashboard widgets:

  * Today's Zone
  * Weekly Rescue Count
  * Trend snippet
  * Alerts
* Provider report:

  * Controller adherence
  * Rescue frequency & patterns
  * Zone distribution
  * Symptom burden
  * Triage incidents
  * Trend charts (1+ time series, 1+ categorical)
* Shareable PDF

---

## 3. Tech Stack

* **Java (Android)**
* **XML Layouts**
* **Firebase Authentication**
* **Firebase Firestore**
* **FCM Notifications** (for parent alerts)
* **JUnit & Mockito** (for Login Presenter testing — R1 requirement)

---

## 4. Firestore Structure (Summary)

*(Based on Final Firestore Schema document)*

### Top-Level Collections

```
parents/{parentUid}
providers/{providerUid}
children/{childUid}
```

### Child Subcollections

```
children/{childUid}/alerts/{alertId}
children/{childUid}/checkins/{checkinId}
children/{childUid}/incidents/{incidentId}
children/{childUid}/inventory/{inventoryId}
children/{childUid}/medLogs/{logId}
children/{childUid}/reports/{reportId}
children/{childUid}/achievements/summary
```

### Provider Access to Reports

```
children/{childUid}/reports/{reportId}/providerAccess/{providerUid}
```

This enforces controlled visibility per parent-defined permissions.

---

## 5. App Architecture

### MVP (Model–View–Presenter) for Login Module

The Login screen was refactored to follow **MVP**, as required:

* **Model:** Firebase Authentication
* **View:** LoginActivity (UI only)
* **Presenter:** Contains logic, validates input, calls Firebase Model
* **JUnit Tests:** Validate presenter behavior using Mockito


## 6. Testing (JUnit + Mockito)

### Login Presenter Tests

* Valid email/password
* Empty input errors
* Invalid formats
* Firebase callbacks mocked using Mockito
* Maximum line coverage achieved per requirement
* ect

To run tests:

```
Right-click test package → Run Tests
```

---

# 7. Team Responsibilities & Contributions

### Based on:

* Weekend task assignments
* All standup meeting logs 
* Final implementation progress


---

## Prabor — R4 Lead + UI Lead

* Built major UI components for R1 → R4
* Implemented **R4 logic (triage, incident handling)**
* Created R4 **adapters**, flows, and escalations
* Led **UI refinement** across the app
* Fixed layout scaling issues
* Helped with full integration testing + video

---

## Elyas — R3 Lead  + UI Lead

* Full implementation of **R3 logic + UI**
* Created all **R3 adapters**, XML screens & transitions
* Implemented **medicine logs**, technique trainer, streaks + badges
* Integrated **Firestore for medLogs & achievements**
* Performed **login unit testing (JUnit + Mockito)**
* Assisted with Git merging, bug fixes, UI polishing
* Helped produce demo video
* Completed README & submission materials

---

## Keerthanan — R2 + R5 Developer

* Implemented **R2 linking logic** + adapters
* Built **R5 UI** and supporting adapter components
* Assisted with project scheduling and Canva designs
* Supported UI/logic integration

---

## Danish — R6 Developer + Provider UI

* Built **Provider role UI** end-to-end
* Implemented **Firebase logic for R2**
* Created **R6 report logic + adapters**
* Implemented scrollable/dynamic provider screens
* Helped finalize code structure & testing

---

## Mustafa — Documentation + Firebase Schema + R1 Logic

* Set up **GitHub + Jira**
* Built **R1 login/signup logic & adapters**
* Designed the **entire Firebase schema**
* Contributed to documentation + code reviews
* Assisted debugging in **R3 + R4**
* Supported integration testing & video recording

---

## 8. Installation & Setup

### Clone Repository

```
git clone https://github.com/<your-team>/smart-air.git
```

### Open in Android Studio

* File → Open → Select project folder
* Let Gradle sync

### Configure Firebase

1. Add your Google Services JSON to:

   ```
   /app/google-services.json
   ```
2. Enable Authentication (Email/Password)
3. Create Firestore database
4. Import Security Rules (if applicable)

### Run App

* Select emulator or physical device
* Press **Run ▶**

---

## 9. License

Academic; created for **CSCB07 – Software Design (Fall 2025)**.

---
