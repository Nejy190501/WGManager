# WG Manager — Intelligente WG-Verwaltung

> **Kurs:** App Entwicklung mit Android — HWR Berlin  
> **Semester:** SoSe 2026  
> **Dozent:** Prof. Holger Zimmermann

---

## Gruppenmitglieder

Das Projekt wurde von den folgenden Studierenden entwickelt:

Jean Yves Nkwane Ebongue - Matrikelnummer: 77201393552

Samed Cevat Ünal - Matrikelnummer: xxxxxxxxxx

---

## Projektbeschreibung

**WG Manager** ist eine Android-App zur umfassenden Verwaltung einer Wohngemeinschaft.  
Die App ermöglicht Mitbewohnern, alle organisatorischen Aspekte des Zusammenlebens digital abzubilden — von der Einkaufsplanung über den Putzplan bis hin zur Finanzverwaltung.

### Kernfeatures

- **Authentifizierung** — Firebase Email/Passwort-Login, Registrierung, Passwort-Zurücksetzen, 2FA
- **Dashboard** — Statusanzeige aller Mitbewohner, Quick-Actions, nächstes Event
- **Einkaufsmanagement** — Gemeinsame Einkaufsliste, Preise, Kostenaufteilung (faire Bilanz), Schulden begleichen
- **Putzplan** — Aufgaben-Rotation zwischen Mitbewohnern, Streak-System, XP-Punkte
- **Kalender** — Gemeinsamer WG-Kalender mit Event-Typen (Party, Ruhezeit, Besuch)
- **Essensplaner** — Wochenplan mit Rezepten und automatischer Zutatenliste für den Einkauf
- **Vorratskammer** — Bestandsverwaltung (Voll/Niedrig/Leer)
- **Fixkosten** — Monatliche Kosten mit automatischer Aufteilung pro Kopf
- **Tresor (Vault)** — Gemeinsame sensible Daten (WiFi-Passwort, IBAN, Türcode)
- **Belohnungs-Shop** — Punkte einlösen für WG-Vorteile
- **Wall of Fame** — Leaderboard mit Kudos/Shame-System und Abzeichen
- **Schwarzes Brett** — Beschwerden, Lob und Umfragen mit Abstimmung
- **Gäste-Pässe** — QR-artige Zugangs-Codes für Besucher mit WiFi-Freigabe
- **Smart Home** — Szenen-Steuerung (Movie Night, Party Mode, Study Time etc.)
- **Crew-Verwaltung** — Mitgliederliste, Rollen, WG-Beitritt per Code
- **Onboarding** — Geführte Schritte für neue Mitbewohner
- **Super-Admin-Panel** — WG-übergreifende Verwaltung, Impersonation, Wartungsmodus
- **Analytics** — Statistiken zu erledigten Aufgaben, Ausgaben, Top-Beitragende
- **WG-Finder** — Öffentliche WG-Suche mit Ausstattung und Beitrittsanfragen

---

## Technischer Aufbau

### Tech-Stack

| Komponente              | Technologie                                |
| ----------------------- | ------------------------------------------ |
| Sprache                 | Kotlin 2.2.10                              |
| UI-Framework            | Jetpack Compose (Material 3)               |
| Architektur             | Single-Activity, zustandsbasierte Navigation |
| Cloud-Datenbank         | Firebase Realtime Database                 |
| Authentifizierung       | Firebase Authentication (Email/Passwort)   |
| Lokale Datenbank        | SQLite (android.database.sqlite)           |
| Min. Android-Version    | API 26 (Android 8.0)                       |
| Build-System            | Gradle (Kotlin DSL), AGP 9.0.0            |


**Ablauf:**
1. Beim App-Start werden Daten von **Firebase** geladen (Cloud)
2. Bei Erfolg: Daten werden in **SQLite** gecacht (Offline-Backup)
3. Bei Firebase-Fehler: Daten werden aus dem **SQLite-Cache** geladen (Offline-Modus)
4. Bei jeder Änderung: Es wird **parallel** in Firebase UND SQLite geschrieben

### Projektstruktur

```
app/src/main/java/com/example/wgmanager/
├── MainActivity.kt                  # Einstiegspunkt, Navigation, Theme
├── data/
│   ├── DataStore.kt                 # Zentraler Datenspeicher (16 Datenklassen, 10 Enums)
│   ├── FirebaseSync.kt              # Firebase Realtime DB Operationen
│   ├── FirebaseAuthManager.kt       # Firebase Authentication Wrapper
│   ├── LocalDatabase.kt             # SQLite Offline-Cache (15 Tabellen)
│   ├── Localization.kt              # DE/EN Übersetzungen
│   └── PdfExporter.kt               # PDF-Export für Einkaufslisten
├── ui/
│   ├── navigation/AppScreen.kt      # Enum aller 20 Screens
│   ├── components/CommonComponents.kt # Wiederverwendbare UI-Bausteine
│   ├── theme/                        # Material 3 Theme, Farben, Typographie
│   └── screens/                      # 20 Bildschirme (je eine Datei)
│       ├── LoginScreen.kt           # Auth-Flow (Login/Register/2FA)
│       ├── DashboardScreen.kt       # Hauptbildschirm nach Login
│       ├── ShoppingScreen.kt        # Einkauf + Finanzen + Vorrat
│       ├── CleaningScreen.kt        # Putzplan mit Rotation
│       ├── CalendarScreen.kt        # WG-Kalender
│       ├── MealPlannerScreen.kt     # Essensplan + Rezepte
│       ├── VaultScreen.kt           # Gemeinsame Passwörter/IBAN
│       ├── RewardsScreen.kt         # Belohnungs-Shop
│       ├── WallOfFameScreen.kt      # Leaderboard
│       ├── BlackboardScreen.kt      # Beschwerden & Umfragen
│       ├── CrewScreen.kt            # Mitglieder-Verwaltung
│       ├── RecurringCostsScreen.kt  # Fixkosten
│       ├── GuestPassScreen.kt       # Gäste-Pässe
│       ├── SmartHomeScreen.kt       # Smart-Home-Szenen
│       ├── AnalyticsScreen.kt       # Statistiken
│       ├── ProfileScreen.kt         # Profil & Einstellungen
│       ├── WGFinderScreen.kt        # WG suchen & beitreten
│       ├── OnboardingScreen.kt      # Neue Mitbewohner einführen
│       ├── SystemPanelScreen.kt     # Super-Admin Panel
│       └── SplashScreen.kt          # Ladebildschirm
```

### Datenmodelle (16 Data Classes)

| Klasse          | Beschreibung                              |
| --------------- | ----------------------------------------- |
| `User`          | Benutzer mit Rolle, Punkte, Status, Theme |
| `WG`            | Wohngemeinschaft mit Regeln & Budget      |
| `ShoppingItem`  | Einkaufs-Artikel mit Preis & Käufer       |
| `Task`          | Putzaufgabe mit Zuweisung & Streak        |
| `Ticket`        | Beschwerde, Lob oder Umfrage              |
| `CalendarEvent` | Kalender-Ereignis mit Typ & Emoji         |
| `Recipe`        | Rezept mit Zutaten & Schwierigkeit        |
| `MealPlanDay`   | Ein Tag im Essensplan                     |
| `VaultItem`     | Gespeichertes Geheimnis (WiFi, IBAN etc.) |
| `RewardItem`    | Belohnung im Punkteshop                   |
| `JoinRequest`   | Beitrittsanfrage an eine WG               |
| `RecurringCost` | Monatliche Fixkosten                      |
| `GuestPass`     | Gäste-Zugangscode                         |
| `SmartScene`    | Smart-Home-Szene                          |
| `OnboardingItem`| Onboarding-Schritt                        |
| `PantryItem`    | Vorratskammer-Artikel                     |

---

## Firebase-Konfiguration

1. **Firebase Console** → [console.firebase.google.com](https://console.firebase.google.com)
2. Projekt: `wgmanager-5df4f`
3. **Authentication** → Email/Passwort aktivieren
4. **Realtime Database** → Region: `europe-west1`
5. DB-URL: `https://wgmanager-5df4f-default-rtdb.europe-west1.firebasedatabase.app/`
6. Sicherheitsregeln (Testmodus):
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

---

## APK bauen

```bash
# Debug-APK (Android Studio)
Build → Build Bundle(s) / APK(s) → Build APK(s)
# Ausgabe: app/build/outputs/apk/debug/app-debug.apk

# Oder via Terminal:
./gradlew assembleDebug
```

---

## Ablaufdiagramm

Siehe [ABLAUFDIAGRAMM.md](ABLAUFDIAGRAMM.md) für das vollständige Ablaufdiagramm mit Mermaid-Diagrammen.

---

## Quellen

- https://michaelkipp.de/android/intro.html
- https://michaelkipp.de/android/erste-app.html
- https://developer.android.com/kotlin?hl=de
- https://kotlinlang.org/
- https://www.imaginarycloud.com/blog/kotlin-vs-java#:~:text=Kotlin%20is%20more%20modern%2C%20with,depends%20on%20your%20project%20needs.
- KI als Unterstützung bei der Fehlersuche (ChatGPT, Google Gemini)
- https://aistudio.google.com/
- https://www.youtube.com/playlist?list=PLYx38U7gxBf3pmsHVTUwRT_lGON6ZIBHi
- https://www.youtube.com/watch?v=N8p7IJiwSLA
