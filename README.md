# WG Manager — Intelligente WG-Verwaltung

> **Kurs:** App Entwicklung mit Android — HWR Berlin  
> **Semester:** SoSe 2026  
> **Dozent:** Prof. Holger Zimmermann

---

## Gruppenmitglieder

Das Projekt wurde von **Jean Yves Nkwane Ebongue - Matrikelnummer: 77201393552** entwickelt:

---

## Projektbeschreibung

**WG Manager** ist eine Android-App zur umfassenden Verwaltung einer Wohngemeinschaft.  
Die App ermöglicht Mitbewohnern, alle organisatorischen Aspekte des Zusammenlebens digital abzubilden — von der Einkaufsplanung über den Putzplan bis hin zur Finanzverwaltung.

### Kernfeatures

- **Authentifizierung (Demo-fähig)** — Muster-Zugangsdaten pro Rolle (User/Admin/Super Admin), Login/Registrierung/Passwort-Reset/2FA-UI
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
| Cloud-Datenbank         | Firebase Realtime Database (teilweise angebunden) |
| Authentifizierung       | Muster-Login mit Demo-Accounts (Firebase Auth nur teilweise angebunden) |
| Lokale Datenbank        | SQLite nur vorbereitet (aktuell nicht aktiv genutzt) |
| Min. Android-Version    | API 26 (Android 8.0)                       |
| Build-System            | Gradle (Kotlin DSL), AGP 9.0.0            |


**Ablauf:**
1. Beim App-Start wird versucht, Daten von **Firebase** zu laden
2. Bei Erfolg: Daten werden im App-State bereitgestellt (Demo-/Projektstand)
3. Bei Firebase-Fehler: Daten werden aus Mock-Daten geladen (Demo-/Offline-Modus)
4. SQLite war als lokaler Cache vorbereitet, wurde im aktuellen Projektstand aber nicht aktiv genutzt

### Projektstruktur

```
app/src/main/java/com/example/wgmanager/
├── MainActivity.kt                  # Einstiegspunkt, Navigation, Theme
├── data/
│   ├── DataStore.kt                 # Zentraler Datenspeicher (16 Datenklassen, 10 Enums)
│   ├── FirebaseSync.kt              # Firebase Realtime DB Operationen
│   ├── FirebaseAuthManager.kt       # Firebase Authentication Wrapper
│   ├── LocalDatabase.kt             # Vorbereitung fuer lokalen SQLite-Cache (aktuell nicht aktiv genutzt)
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

## Aktueller Firebase-Status

Die vollständige Ende-zu-Ende-Anbindung an Firebase wurde im Projektzeitraum nicht fertiggestellt.  
Für die Demonstration verwenden wir deshalb vordefinierte Muster-Zugangsdaten pro Rolle.

### Muster-Zugangsdaten

> Passwort für die Demo-Accounts: `1234`

| Rolle | E-Mail | Hinweis |
| ----- | ------ | ------- |
| User | `max@wg.com` | Standard-Mitbewohner mit WG |
| Admin | `admin@wg.com` | Admin-Rechte in der WG |
| Super Admin | `super@wg.com` | Zugriff auf System-Panel |
| User (ohne WG) | `new@wg.com` | Startet im WG-Finder/Onboarding |

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
- https://aistudio.google.com/
- https://www.youtube.com/playlist?list=PLYx38U7gxBf3pmsHVTUwRT_lGON6ZIBHi
- https://www.youtube.com/watch?v=N8p7IJiwSLA
- https://michaelkipp.de/android/hilfe.html
- https://michaelkipp.de/android/intro.html
- https://michaelkipp.de/android/erste-app.html
- https://michaelkipp.de/android/layout.html
- https://michaelkipp.de/android/interaktion.html
- https://michaelkipp.de/android/ressourcen.html
- https://michaelkipp.de/android/daten.html
- https://michaelkipp.de/android/daten2.html
- https://michaelkipp.de/android/intents.html
- https://michaelkipp.de/android/layout2.html
- KI als Unterstützung bei der Fehlersuche und Erweiterung von Ideen (ChatGPT, Google Gemini)
