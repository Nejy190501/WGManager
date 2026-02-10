package com.example.wgmanager.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Calendar

// ═══════════════════════════════════════════════════════════════
// LOCALIZATION SYSTEM - German (DE) & English (EN)
// ═══════════════════════════════════════════════════════════════

enum class AppLanguage { DE, EN }

val LocalAppLanguage = compositionLocalOf { mutableStateOf(AppLanguage.DE) }

object AppStrings {
    // ─── General ───────────────────────────────────────────────
    val appName get() = s("WG Manager", "WG Manager")
    val done get() = s("Fertig", "Done")
    val cancel get() = s("Abbrechen", "Cancel")
    val save get() = s("Speichern", "Save")
    val close get() = s("Schließen", "Close")
    val delete get() = s("Löschen", "Delete")
    val add get() = s("Hinzufügen", "Add")
    val edit get() = s("Bearbeiten", "Edit")
    val yes get() = s("Ja", "Yes")
    val no get() = s("Nein", "No")
    val ok get() = s("OK", "OK")
    val loading get() = s("Laden...", "Loading...")
    val error get() = s("Fehler", "Error")
    val success get() = s("Erfolg", "Success")
    val back get() = s("Zurück", "Back")
    val next get() = s("Weiter", "Next")

    // ─── Login/Register ────────────────────────────────────────
    val goodEvening get() = s("Guten Abend", "Good Evening")
    val goodMorning get() = s("Guten Morgen", "Good Morning")
    val goodAfternoon get() = s("Guten Nachmittag", "Good Afternoon")
    val greeting: String get() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> goodMorning
            hour in 12..17 -> goodAfternoon
            else -> goodEvening
        }
    }
    val loginSubtitle get() = s("Melde dich bei deiner WG an.", "Sign in to your flat")
    val joinTheClub get() = s("Tritt dem Club bei", "Join the Club")
    val registerSubtitle get() = s("Erstelle ein Konto, um einer WG beizutreten.", "Create an account to join or start a WG.")
    val email get() = s("E-Mail Adresse", "Email Address")
    val password get() = s("Passwort", "Password")
    val newPassword get() = s("Neues Passwort", "New Password")
    val name get() = s("Vollständiger Name", "Full Name")
    val rememberMe get() = s("Angemeldet bleiben", "Remember me")
    val forgotPassword get() = s("Passwort vergessen?", "Forgot password?")
    val login get() = s("Anmelden", "Sign In")
    val register get() = s("Konto erstellen", "Create Account")
    val noAccount get() = s("Neu beim WG Manager?", "New to WG Manager?")
    val haveAccount get() = s("Hast du schon ein Konto?", "Already have an account?")
    val registerLink get() = s("Registrieren", "Sign Up")
    val newToApp get() = s("Neu beim WG Manager?", "New to WG Manager?")
    val demoMode get() = s("DEMO-MODUS", "DEMO MODE")
    val invalidCredentials get() = s("Ungültige Anmeldedaten", "Invalid credentials")
    val allFieldsRequired get() = s("Alle Felder erforderlich", "All fields required")
    val acceptTerms get() = s("Bitte AGB akzeptieren", "Please accept terms")
    val emailExists get() = s("E-Mail bereits vorhanden", "Email already exists")
    val accountCreated get() = s("Konto erstellt!", "Account created!")
    val welcomeBack get() = s("Willkommen zurück", "Welcome back")
    val agreeToTerms get() = s("Ich stimme den ", "I agree to ")
    val termsOfService get() = s("AGB", "Terms")
    val and get() = s("&", "&")
    val privacyPolicy get() = s("Datenschutz zu", "Privacy Policy")
    val cookieBanner get() = s("Wir nutzen Cookies für die Funktionalität.", "We use cookies for essential functionality.")
    val accept get() = s("Alles klar", "Got it")
    val selectAccount get() = s("Konto auswählen:", "Select account:")

    // ─── Password Reset ────────────────────────────────────────
    val resetPassword get() = s("Passwort zurücksetzen", "Reset Password")
    val resetEmailSent get() = s("Wir senden dir eine E-Mail zum Zurücksetzen.", "We'll email you instructions to reset your password.")
    val sendLink get() = s("Link senden", "Send Link")
    val backToLogin get() = s("Zurück zum Login", "Back to Login")
    val linkSentTo get() = s("Link gesendet an", "Link sent to")
    val enterValidEmail get() = s("Bitte gib zuerst eine gültige E-Mail ein.", "Please enter a valid email first.")

    // ─── Password Strength ─────────────────────────────────────
    val weak get() = s("Schwach", "Weak")
    val good get() = s("Gut", "Good")
    val strong get() = s("Stark", "Strong")

    // ─── 2FA ───────────────────────────────────────────────────
    val twoFactorAuth get() = s("2-Faktor Authentifizierung", "Two-Factor Authentication")
    val enterCode get() = s("Gib den 6-stelligen Code ein", "Enter the 6-digit code")
    val codeSentTo get() = s("Code gesendet an", "Code sent to")
    val verify get() = s("Verifizieren", "Verify")
    val resendCode get() = s("Code erneut senden", "Resend code")
    val invalidCode get() = s("Ungültiger Code", "Invalid code")
    val codeVerified get() = s("Code verifiziert!", "Code verified!")

    // ─── Terms & Privacy Content ──────────────────────────────
    val termsTitle get() = s("Allgemeine Geschäftsbedingungen (AGB)", "Terms of Service (ToS)")
    val termsContent get() = s(
        "§1 Geltungsbereich\nDiese Allgemeinen Geschäftsbedingungen gelten für die Nutzung der WG Manager App.\n\n§2 Nutzungsbedingungen\n• Die App darf nur für legale Zwecke genutzt werden.\n• Jeder Nutzer ist für die Sicherheit seines Kontos verantwortlich.\n• Geteilte Daten (Einkaufslisten, Finanzen) sind nur für WG-Mitglieder sichtbar.\n\n§3 Pflichten der Nutzer\n1. Respektiere deine Mitbewohner.\n2. Erledige deine zugeteilten Aufgaben pünktlich.\n3. Bezahle deine Schulden innerhalb von 14 Tagen.\n4. Halte die Gemeinschaftsräume sauber.\n5. Keine Beleidigungen oder Diskriminierung.\n\n§4 Haftungsausschluss\nDie App dient als Organisationshilfe. Für tatsächliche Schäden übernehmen wir keine Haftung.\n\n§5 Kündigung\nJeder Nutzer kann sein Konto jederzeit löschen. Die verbundenen Daten werden dabei entfernt.",
        "§1 Scope\nThese Terms of Service apply to the use of the WG Manager app.\n\n§2 Terms of Use\n• The app may only be used for legal purposes.\n• Each user is responsible for the security of their account.\n• Shared data (shopping lists, finances) is only visible to WG members.\n\n§3 User Obligations\n1. Respect your flatmates.\n2. Complete your assigned tasks on time.\n3. Pay your debts within 14 days.\n4. Keep common areas clean.\n5. No insults or discrimination.\n\n§4 Disclaimer\nThe app serves as an organizational tool. We assume no liability for actual damages.\n\n§5 Termination\nEvery user can delete their account at any time. The associated data will be removed."
    )
    val privacyTitle get() = s("Datenschutzerklärung", "Privacy Policy")
    val privacyContent get() = s(
        "1. Verantwortlicher\nWG Manager App – Kontakt: support@wg-manager.app\n\n2. Welche Daten wir erheben\n• Registrierungsdaten: Name, E-Mail-Adresse\n• Nutzungsdaten: Aufgaben, Einkäufe, Kalendereinträge\n• Technische Daten: Gerätetyp, App-Version\n\n3. Zweck der Datenverarbeitung\n• Bereitstellung der App-Funktionalitäten\n• Synchronisation zwischen WG-Mitgliedern\n• Verbesserung der Nutzererfahrung\n\n4. Datenspeicherung\n• Daten werden sicher in Firebase (Google Cloud) gespeichert.\n• Keine Weitergabe an Dritte.\n• Verschlüsselte Übertragung (SSL/TLS).\n\n5. Deine Rechte (DSGVO)\n• Recht auf Auskunft über deine Daten\n• Recht auf Löschung deiner Daten\n• Recht auf Datenportabilität\n• Widerspruchsrecht\n\n6. Kontakt\nBei Fragen zum Datenschutz: datenschutz@wg-manager.app",
        "1. Data Controller\nWG Manager App – Contact: support@wg-manager.app\n\n2. Data We Collect\n• Registration data: name, email address\n• Usage data: tasks, purchases, calendar entries\n• Technical data: device type, app version\n\n3. Purpose of Data Processing\n• Providing app functionalities\n• Synchronization between WG members\n• Improving user experience\n\n4. Data Storage\n• Data is stored securely in Firebase (Google Cloud).\n• No sharing with third parties.\n• Encrypted transmission (SSL/TLS).\n\n5. Your Rights (GDPR)\n• Right to access your data\n• Right to delete your data\n• Right to data portability\n• Right to object\n\n6. Contact\nFor privacy questions: privacy@wg-manager.app"
    )

    // ─── Dashboard ────────────────────────────────────────────
    val dashboard get() = s("Dashboard", "Dashboard")
    val hello get() = s("Moin", "Hi")
    val activeNow get() = s("AKTIV JETZT", "ACTIVE NOW")
    val shoppingList get() = s("Einkaufsliste", "Shopping List")
    val openItems get() = s("offene Artikel", "open items")
    val tasks get() = s("Aufgaben", "Tasks")
    val openTasks get() = s("offene Aufgaben", "open tasks")
    val yourTurn get() = s("Du bist dran!", "Your turn!")
    val blackboard get() = s("Schwarzes Brett", "Blackboard")
    val newNotes get() = s("neue Notizen", "new notes")
    val toolsAndTeam get() = s("TOOLS & TEAM", "TOOLS & TEAM")
    val cleaning get() = s("Putzplan", "Cleaning")
    val calendar get() = s("Kalender", "Calendar")
    val mealPlan get() = s("Essensplan", "Meal Plan")
    val finances get() = s("Finanzen", "Finances")
    val vault get() = s("Tresor", "Vault")
    val stats get() = s("Statistiken", "Statistics")
    val crew get() = s("Crew", "Crew")
    val rewards get() = s("Prämien", "Rewards")

    // ─── Status ───────────────────────────────────────────────
    val setStatus get() = s("Status setzen", "Set Status")
    val online get() = s("Online", "Online")
    val sleeping get() = s("Schlafen", "Sleeping")
    val focus get() = s("Fokus", "Focus")
    val party get() = s("Party", "Party")
    val shower get() = s("Duschen", "Shower")
    val away get() = s("Abwesend", "Away")
    val studying get() = s("Lernen", "Studying")
    val muted get() = s("Stumm", "Muted")
    val cooking get() = s("Kochen", "Cooking")
    val working get() = s("Arbeiten", "Working")

    // ─── Shopping ──────────────────────────────────────────────
    val list get() = s("Liste", "List")
    val balance get() = s("Bilanz", "Balance")
    val monthlyBudget get() = s("Monatsbudget", "Monthly Budget")
    val total get() = s("Gesamt", "Total")
    val openLabel get() = s("Offen", "Open")
    val quickAdd get() = s("SCHNELLWAHL", "QUICK ADD")
    val addItem get() = s("Artikel hinzufügen...", "Add item...")
    val addedBy get() = s("HINZUGEFÜGT VON", "ADDED BY")
    val expenseDistribution get() = s("Ausgabenverteilung", "Expense Distribution")
    val basedOnActivity get() = s("Basierend auf Aktivität", "Based on activity")
    val paid get() = s("Bezahlt", "Paid")
    val receives get() = s("BEKOMMT", "RECEIVES")
    val owes get() = s("SCHULDET", "OWES")
    val settleDebts get() = s("Schulden begleichen", "Settle Debts")
    val settleDebtsWho get() = s("Wen bezahlst du?", "Who are you paying?")
    val settleDebtsPaid get() = s("Bezahlt an", "Paid to")
    val noDebts get() = s("Keine offenen Schulden!", "No open debts!")
    val settleConfirmTitle get() = s("Zahlung bestätigen", "Confirm Payment")
    val settleConfirmMsg get() = s("Schulden an %s über %s begleichen?", "Settle debt to %s of %s?")
    val settleSuccess get() = s("Schulden beglichen! ✅", "Debts settled! ✅")
    val adminOnly get() = s("Nur Admins können das", "Only admins can do this")

    // ─── Cleaning ──────────────────────────────────────────────
    val week get() = s("Woche", "Week")
    val addTask get() = s("Aufgabe hinzufügen", "Add Task")
    val nudge get() = s("Anstupsen", "Nudge")
    val strike get() = s("Strike", "Strike")
    val assignee get() = s("Zuständig", "Assignee")

    // ─── Profile ───────────────────────────────────────────────
    val profile get() = s("Profil", "Profile")
    val overview get() = s("Übersicht", "Overview")
    val badges get() = s("Abzeichen", "Badges")
    val settings get() = s("Einstellungen", "Settings")
    val currentRank get() = s("AKTUELLER RANG", "CURRENT RANK")
    val xpNeeded get() = s("XP bis zum nächsten Level", "XP to next level")
    val thisMonth get() = s("DIESEN MONAT", "THIS MONTH")
    val completed get() = s("ERLEDIGT", "COMPLETED")
    val spending get() = s("AUSGABEN", "SPENDING")
    val streak get() = s("SERIE", "STREAK")
    val speed get() = s("TEMPO", "SPEED")
    val unlocked get() = s("Freigeschaltet", "Unlocked")
    val locked get() = s("Gesperrt", "Locked")

    // ─── Settings ──────────────────────────────────────────────
    val language get() = s("Sprache", "Language")
    val german get() = s("Deutsch", "German")
    val english get() = s("Englisch", "English")
    val notifications get() = s("Benachrichtigungen", "Notifications")
    val darkMode get() = s("Dunkelmodus", "Dark Mode")
    val appDesign get() = s("App Design", "App Design")
    val security get() = s("Sicherheit", "Security")
    val twoFA get() = s("2FA Authentifizierung", "2FA Authentication")
    val changePassword get() = s("Passwort ändern", "Change Password")
    val legalAndHelp get() = s("RECHTLICHES & HILFE", "LEGAL & HELP")
    val helpCenter get() = s("Hilfe Center", "Help Center")
    val termsLabel get() = s("Nutzungsbedingungen", "Terms of Service")
    val privacyLabel get() = s("Datenschutz", "Privacy")
    val account get() = s("Konto", "Account")
    val joinedOn get() = s("Beigetreten", "Joined")
    val logout get() = s("Abmelden", "Logout")

    // ─── Rewards ───────────────────────────────────────────────
    val rewardsShop get() = s("Prämien Shop", "Rewards Shop")
    val offers get() = s("Angebote", "Offers")
    val myInventory get() = s("Mein Inventar", "My Inventory")
    val yourPoints get() = s("Deine Punkte", "Your Points")
    val buy get() = s("Kaufen", "Buy")
    val points get() = s("Punkte", "Points")
    val notEnoughPoints get() = s("Nicht genug Punkte", "Not enough points")

    // ─── Analytics ────────────────────────────────────────────
    val analytics get() = s("Statistiken", "Analytics")
    val tasksTab get() = s("Aufgaben", "Tasks")
    val financesTab get() = s("Finanzen", "Finances")
    val distribution get() = s("Verteilung", "Distribution")
    val trend get() = s("Ausgabenverlauf", "Spending Trend")
    val categories get() = s("Kategorien", "Categories")
    val topContributor get() = s("Top Beitragender", "Top Contributor")
    val topSpender get() = s("Top Einkäufer", "Top Spender")

    // ─── Theme Picker ──────────────────────────────────────────
    val chooseDesign get() = s("Wähle dein Design", "Choose your Design")
    val neonNight get() = s("Neon Night", "Neon Night")
    val freshMint get() = s("Fresh Mint", "Fresh Mint")
    val sweetCandy get() = s("Sweet Candy", "Sweet Candy")
    val sunset get() = s("Sunset", "Sunset")
    val ocean get() = s("Ocean", "Ocean")

    // ─── Help Dialog ───────────────────────────────────────────
    val help get() = s("Hilfe", "Help")
    val helpText get() = s("Frag deinen Admin oder schreib an support@wg.com", "Ask your admin or write to support@wg.com")

    // ─── Member Profile ────────────────────────────────────────
    val memberSince get() = s("Mitglied seit", "Member since")
    val role get() = s("Rolle", "Role")
    val admin get() = s("Admin", "Admin")
    val user get() = s("Benutzer", "User")
    val sendMessage get() = s("Nachricht senden", "Send Message")
    val viewProfile get() = s("Profil ansehen", "View Profile")

    // ─── Quick Status Presets ──────────────────────────────────
    val quickStatus get() = s("Schnellstatus", "Quick Status")
    val noWifi get() = s("Kein WLAN", "No WiFi")
    val noWater get() = s("Kein Wasser", "No Water")
    val partyTonight get() = s("Party heute Abend!", "Party tonight!")
    val quietPlease get() = s("Ruhe bitte!", "Quiet please!")
    val guestsOver get() = s("Besuch da", "Guests over")

    // ─── Avatar Selection ──────────────────────────────────────
    val selectAvatar get() = s("Avatar auswählen", "Select Avatar")
    val useInitials get() = s("Initialen verwenden", "Use Initials")
    val importPhoto get() = s("Foto importieren", "Import Photo")
    val maleAvatars get() = s("Männlich", "Male")
    val femaleAvatars get() = s("Weiblich", "Female")

    // ─── Toasts/Notifications ──────────────────────────────────
    val itemAdded get() = s("Artikel hinzugefügt!", "Item added!")
    val taskCompleted get() = s("Aufgabe erledigt!", "Task completed!")
    val profileUpdated get() = s("Profil aktualisiert!", "Profile updated!")
    val passwordChanged get() = s("Passwort geändert!", "Password changed!")
    val languageChanged get() = s("Sprache geändert", "Language changed")
    val designChanged get() = s("Design geändert", "Design changed")
    val statusUpdated get() = s("Status aktualisiert", "Status updated")
    val nudgeSent get() = s("Erinnerung gesendet!", "Nudge sent!")
    val rewardPurchased get() = s("Prämie gekauft!", "Reward purchased!")

    // ─── Splash Screen ────────────────────────────────────────
    val tagline get() = s("ZUSAMMEN • EINFACH • BESSER", "TOGETHER • SIMPLE • BETTER")

    // ─── WG Finder ────────────────────────────────────────────
    val wgJoinTitle get() = s("WG beitreten", "Join a WG")
    val wgJoinSubtitle get() = s("Gib einen Einladungscode ein oder suche in der Nähe.", "Enter an invitation code or browse nearby.")
    val haveACode get() = s("HAST DU EINEN CODE?", "GOT A CODE?")
    val codePlaceholder get() = s("z.B. SUNNY", "e.g. SUNNY")
    val joinBtn get() = s("BEITRETEN", "JOIN")
    val suggestionsNearby get() = s("VORSCHLÄGE IN DER NÄHE", "SUGGESTIONS NEARBY")
    val filter get() = s("Filter", "Filter")
    val viewDetails get() = s("Details ansehen", "View details")
    val aboutUs get() = s("ÜBER UNS", "ABOUT US")
    val amenitiesLabel get() = s("AUSSTATTUNG", "AMENITIES")
    val sendRequest get() = s("Anfrage senden", "Send request")
    val requestSent get() = s("Anfrage gesendet", "Request sent")
    val searchingOnline get() = s("Suchende online", "searching online")
    val invalidCodeMsg get() = s("Ungültiger Code", "Invalid code")
    val codeEmptyMsg get() = s("Bitte gib einen Code ein", "Please enter a code")
    val perRoom get() = s("PRO ZIMMER", "PER ROOM")
    val kmAway get() = s("entfernt", "away")

    // ─── Join Requests & Admin ────────────────────────────────
    val pendingRequests get() = s("Anfragen", "Requests")
    val noPendingRequests get() = s("Keine Anfragen", "No requests")
    val acceptRequest get() = s("Akzeptieren", "Accept")
    val rejectRequest get() = s("Ablehnen", "Reject")
    val requestAccepted get() = s("Anfrage akzeptiert", "Request accepted")
    val requestRejected get() = s("Anfrage abgelehnt", "Request rejected")
    val requestMessage get() = s("Nachricht (optional)", "Message (optional)")
    val sendYourRequest get() = s("Anfrage senden", "Send your request")
    val yourMessage get() = s("Deine Nachricht...", "Your message...")
    val editWGShowcase get() = s("WG Vitrine bearbeiten", "Edit WG Showcase")
    val editMyProfile get() = s("Mein Profil bearbeiten", "Edit my profile")
    val rentPerRoom get() = s("Miete pro Zimmer (€)", "Rent per room (€)")
    val publicDescription get() = s("Öffentliche Beschreibung", "Public description")
    val showcaseUpdated get() = s("Vitrine aktualisiert!", "Showcase updated!")
    val wgInfosBearbeiten get() = s("WG Infos bearbeiten", "Edit WG Info")
    val monatlicheMiete get() = s("MONATLICHE MIETE (€)", "MONTHLY RENT (€)")
    val oeffentlicheBeschreibung get() = s("ÖFFENTLICHE BESCHREIBUNG", "PUBLIC DESCRIPTION")
    val hausregelnFuerOnboarding get() = s("HAUSREGELN (FÜR ONBOARDING)", "HOUSE RULES (FOR ONBOARDING)")
    val adminZone get() = s("ADMIN ZONE", "ADMIN ZONE")
    val wgEinstellungen get() = s("WG Einstellungen", "WG Settings")
    val wgInfosUpdated get() = s("WG Infos aktualisiert!", "WG info updated!")
    val membersList get() = s("MITGLIEDER", "MEMBERS")
    val alreadyRequested get() = s("Anfrage bereits gesendet", "Request already sent")
    val requestSentSuccess get() = s("Anfrage erfolgreich gesendet!", "Request sent successfully!")
    val whatToEdit get() = s("Was möchtest du bearbeiten?", "What would you like to edit?")

    // ─── Calendar ─────────────────────────────────────────────
    val addEvent get() = s("Ereignis hinzufügen", "Add Event")
    val eventTitle get() = s("Titel", "Title")
    val eventDate get() = s("Datum", "Date")
    val eventType get() = s("Typ", "Type")

    // ─── Blackboard ───────────────────────────────────────────
    val newNote get() = s("Neue Notiz", "New Note")
    val complaint get() = s("Beschwerde", "Complaint")
    val kudos get() = s("Lob", "Kudos")
    val poll get() = s("Umfrage", "Poll")

    // ─── Recurring Costs ──────────────────────────────────────
    val recurringCosts get() = s("Fixkosten", "Fixed Costs")
    val recurringCostsSubtitle get() = s("Monatliche Kosten aufgeteilt", "Monthly costs split evenly")
    val totalMonthly get() = s("Gesamt / Monat", "Total / Month")
    val perPerson get() = s("Pro Person", "Per Person")
    val paidBy get() = s("Bezahlt von", "Paid by")
    val addCost get() = s("Kosten hinzufügen", "Add Cost")
    val costName get() = s("Bezeichnung", "Name")
    val costAmount get() = s("Betrag (€)", "Amount (€)")
    val costEmoji get() = s("Emoji", "Emoji")
    val activeCosts get() = s("AKTIVE KOSTEN", "ACTIVE COSTS")
    val noCosts get() = s("Noch keine Fixkosten erfasst", "No fixed costs added yet")
    val costAdded get() = s("Kostenpunkt hinzugefügt!", "Cost added!")
    val costRemoved get() = s("Kostenpunkt entfernt", "Cost removed")
    val monthly get() = s("Monatlich", "Monthly")
    val weekly get() = s("Wöchentlich", "Weekly")

    // ─── Wall of Fame ─────────────────────────────────────────
    val wallOfFame get() = s("Wall of Fame", "Wall of Fame")
    val wallOfFameSubtitle get() = s("Wer glänzt, wer pennt?", "Who shines, who slacks?")
    val leaderboard get() = s("RANGLISTE", "LEADERBOARD")
    val kudosSent get() = s("Kudos gesendet!", "Kudos sent!")
    val shameSent get() = s("Shame gesendet!", "Shame sent!")
    val sendKudos get() = s("Kudos senden", "Send Kudos")
    val sendShame get() = s("Shame senden", "Send Shame")
    val pointsLabel get() = s("Punkte", "Points")
    val crownHolder get() = s("König/in der WG", "WG Champion")
    val clownHolder get() = s("Faulpelz der WG", "WG Slacker")

    // ─── Guest Pass ───────────────────────────────────────────
    val guestPass get() = s("Gästepass", "Guest Pass")
    val guestPassSubtitle get() = s("QR-Code für Besucher", "QR code for visitors")
    val createPass get() = s("Pass erstellen", "Create Pass")
    val guestName get() = s("Name des Gastes", "Guest Name")
    val activePassesTitle get() = s("AKTIVE PÄSSE", "ACTIVE PASSES")
    val noActivePasses get() = s("Keine aktiven Gästepässe", "No active guest passes")
    val passCreated get() = s("Gästepass erstellt!", "Guest pass created!")
    val passRevoked get() = s("Pass widerrufen", "Pass revoked")
    val accessCode get() = s("Zugangscode", "Access Code")
    val wifiPassword get() = s("WLAN-Passwort", "WiFi Password")
    val revokePass get() = s("Widerrufen", "Revoke")
    val createdByLabel get() = s("Erstellt von", "Created by")
    val guestInfo get() = s("GAST-INFO", "GUEST INFO")

    // ─── Smart Home ───────────────────────────────────────────
    val smartHome get() = s("Smart Home", "Smart Home")
    val smartHomeSubtitle get() = s("Simulierte Szenen-Steuerung", "Simulated scene controls")
    val scenesTitle get() = s("SZENEN", "SCENES")
    val sceneActivated get() = s("Szene aktiviert!", "Scene activated!")
    val sceneDeactivated get() = s("Szene deaktiviert", "Scene deactivated")
    val addScene get() = s("Szene hinzufügen", "Add Scene")
    val sceneName get() = s("Szenenname", "Scene Name")
    val sceneDescription get() = s("Beschreibung", "Description")
    val notificationText get() = s("Benachrichtigung", "Notification")
    val sceneAdded get() = s("Szene hinzugefügt!", "Scene added!")

    // ─── Onboarding ───────────────────────────────────────────
    val onboarding get() = s("Willkommen!", "Welcome!")
    val onboardingSubtitle get() = s("Deine Einzugs-Checkliste", "Your move-in checklist")
    val onboardingProgress get() = s("Fortschritt", "Progress")
    val readRules get() = s("WG-Regeln lesen", "Read WG rules")
    val addIban get() = s("IBAN hinterlegen", "Add your IBAN")
    val pickCleaningDay get() = s("Putztag wählen", "Pick cleaning day")
    val setAvatar get() = s("Avatar & Profil einrichten", "Set up avatar & profile")
    val introduceSelf get() = s("Stelle dich vor!", "Introduce yourself!")
    val stepCompleted get() = s("Erledigt!", "Done!")
    val onboardingComplete get() = s("Onboarding abgeschlossen! 🎉 +50 Punkte!", "Onboarding complete! 🎉 +50 Points!")
    val markAsDone get() = s("Als erledigt markieren", "Mark as done")

    // ─── Onboarding Pager ─────────────────────────────────────
    val welcomeHome get() = s("Willkommen zuhause!", "Welcome home!")
    val welcomeHomeDesc get() = s("Richten wir dein Profil ein, damit alle Bescheid wissen.", "Let's set up your profile so everyone knows.")
    val hausregeln get() = s("Hausregeln", "House Rules")
    val readyTitle get() = s("Bereit?", "Ready?")
    val readyDesc get() = s("Dein Abenteuer beginnt jetzt. Sammle XP mit Aufgaben!", "Your adventure starts now. Earn XP with tasks!")
    val weiter get() = s("Weiter", "Next")
    val losGehts get() = s("Los geht's!", "Let's go!")

    // ─── Admin: Join Requests ─────────────────────────────────
    val joinRequests get() = s("Beitrittsanfragen", "Join Requests")
    val noJoinRequests get() = s("Keine offenen Anfragen", "No open requests")
    val acceptBtn get() = s("Annehmen", "Accept")
    val rejectBtn get() = s("Ablehnen", "Reject")
    val requestAcceptedMsg get() = s("Anfrage angenommen!", "Request accepted!")
    val requestRejectedMsg get() = s("Anfrage abgelehnt", "Request rejected")

    // ─── Admin: Amenities ─────────────────────────────────────
    val amenitiesManage get() = s("Ausstattung verwalten", "Manage Amenities")
    val amenityWifi get() = s("WLAN", "WiFi")
    val amenityWasher get() = s("Waschmaschine", "Washer")
    val amenityDryer get() = s("Trockner", "Dryer")
    val amenityParking get() = s("Parkplatz", "Parking")
    val amenityBalcony get() = s("Balkon", "Balcony")
    val amenityGarden get() = s("Garten", "Garden")
    val amenityDishwasher get() = s("Spülmaschine", "Dishwasher")
    val amenityElevator get() = s("Aufzug", "Elevator")
    val amenityBikeStorage get() = s("Fahrradkeller", "Bike Storage")
    val amenityCellar get() = s("Keller", "Cellar")
    val amenityBathtub get() = s("Badewanne", "Bathtub")
    val amenityTv get() = s("Fernseher", "TV")
    val amenitiesUpdated get() = s("Ausstattung aktualisiert!", "Amenities updated!")

    // ─── Onboarding descriptions ──────────────────────────────
    val readRulesDesc get() = s("Lies die WG-Regeln aufmerksam durch", "Read the WG ground rules carefully")
    val addIbanDesc get() = s("Für Miete & gemeinsame Kosten", "For rent & shared costs")
    val pickCleaningDayDesc get() = s("Tritt dem Putzplan bei", "Join the cleaning rotation")
    val setAvatarDesc get() = s("Mach dich erkennbar", "Make yourself recognizable")
    val introduceSelfDesc get() = s("Schreib etwas auf die Pinnwand", "Post on the blackboard")
    val noRulesYet get() = s("Noch keine Regeln festgelegt", "No rules set yet")

    // ─── WG Rules & Budget (Admin) ────────────────────────────
    val wgRules get() = s("WG-Regeln", "WG Rules")
    val wgRulesHint get() = s("Regeln für die WG (eine pro Zeile)", "Rules for the WG (one per line)")
    val rulesUpdated get() = s("Regeln aktualisiert!", "Rules updated!")
    val editWGRules get() = s("WG-Regeln bearbeiten", "Edit WG Rules")
    val monthlyBudgetLabel get() = s("Monatsbudget (€)", "Monthly Budget (€)")
    val budgetUpdated get() = s("Budget aktualisiert!", "Budget updated!")
    val chooseEmoji get() = s("Emoji wählen", "Choose Emoji")

    // ─── Shopping / Vorrat (Pantry) ───────────────────────────
    val vorrat get() = s("Vorrat", "Pantry")
    val pantryFull get() = s("Voll", "Full")
    val pantryLow get() = s("Wenig", "Low")
    val pantryEmpty get() = s("Leer", "Empty")
    val pantrySubtitle get() = s("Bestandsverwaltung", "Stock Management")
    val noPantryItems get() = s("Noch keine Vorräte erfasst", "No pantry items yet")
    val addPantryItem get() = s("Vorrat hinzufügen", "Add Pantry Item")
    val pantryItemName get() = s("Bezeichnung", "Item Name")
    val pantryAdded get() = s("Vorrat hinzugefügt!", "Pantry item added!")
    val pantryStatusUpdated get() = s("Status aktualisiert!", "Status updated!")
    val totalOpenLabel get() = s("Gesamt (Offen)", "Total (Open)")
    val noMembersYet get() = s("Noch keine Mitglieder", "No members yet")

    // ─── Permission Request ───────────────────────────────────
    val accessRequest get() = s("Zugriffsanfrage", "Access Request")
    val accessRequestText get() = s(
        "Die App benötigt Zugriff auf folgende Berechtigungen:",
        "The app requests access to the following permissions:"
    )
    val cameraPermission get() = s("Kamera", "Camera")
    val galleryPermission get() = s("Fotogalerie", "Photo Gallery")
    val storagePermission get() = s("Speicher", "Storage")
    val allowAccess get() = s("Zugriff erlauben", "Allow Access")
    val denyAccess get() = s("Ablehnen", "Deny")

    // ─── Password Reset Dialog ────────────────────────────────
    val resetSuccessTitle get() = s("E-Mail gesendet! ✉️", "Email Sent! ✉️")
    val resetSuccessMsg get() = s(
        "Überprüfe dein Postfach und folge den Anweisungen, um dein Passwort zurückzusetzen.",
        "Check your inbox and follow the instructions to reset your password."
    )
    val resetErrorTitle get() = s("Fehler ⚠️", "Error ⚠️")
    val resetErrorMsg get() = s(
        "Bitte gib eine gültige E-Mail-Adresse ein.",
        "Please enter a valid email address."
    )

    // ─── System Panel (Super Admin) ─────────────────────────
    val systemPanel get() = s("System Panel", "System Panel")
    val systemPanelV2 get() = s("SYSTEM_PANEL_V2", "SYSTEM_PANEL_V2")
    val godMode get() = s("GOD MODE", "GOD MODE")
    val spDashboard get() = s("Dashboard", "Dashboard")
    val spUsers get() = s("Users", "Users")
    val spSearchPlaceholder get() = s("Nach Name oder E-Mail suchen...", "Search by name or email...")
    val spLoginAs get() = s("Login As", "Login As")
    val spMakeAdmin get() = s("Make Admin", "Make Admin")
    val spBan get() = s("Ban", "Ban")
    val spUnban get() = s("Unban", "Unban")
    val spDemote get() = s("Demote", "Demote")
    val spPoints get() = s("PUNKTE", "POINTS")
    val spXp get() = s("XP", "XP")
    val spUsersCount get() = s("BENUTZER", "USERS")
    val spWgsCount get() = s("WGs", "WGs")
    val spUptime get() = s("VERFÜGBARKEIT", "UPTIME")
    val spSystemControl get() = s("SYSTEM CONTROL", "SYSTEM CONTROL")
    val spMaintenanceMode get() = s("Maintenance Mode", "Maintenance Mode")
    val spMaintenanceDesc get() = s("App für alle Nutzer sperren", "Lock app for all users")
    val spBroadcastAlert get() = s("Broadcast Alert", "Broadcast Alert")
    val spNukeDatabase get() = s("Nuke Database", "Nuke Database")
    val spLiveConsole get() = s("LIVE CONSOLE", "LIVE CONSOLE")
    val spTerminateSession get() = s("Terminate Session", "Terminate Session")
    val spConfirmNuke get() = s("ALLE Inhalte (Einkäufe, Aufgaben, Tickets usw.) werden gelöscht. Benutzer und WGs bleiben erhalten.\n\nDiese Aktion kann nicht rückgängig gemacht werden!", "ALL content data (shopping, tasks, tickets, etc.) will be deleted. Users and WGs will be preserved.\n\nThis action cannot be undone!")
    val spNukeConfirmTitle get() = s("☢️ Datenbank zurücksetzen?", "☢️ Nuke Database?")
    val spNuked get() = s("Datenbank bereinigt! 🔥", "Database nuked! 🔥")
    val spMaintenanceEnabled get() = s("Maintenance Mode aktiviert 🔒", "Maintenance Mode enabled 🔒")
    val spMaintenanceDisabled get() = s("Maintenance Mode deaktiviert 🔓", "Maintenance Mode disabled 🔓")
    val spBroadcastSent get() = s("Broadcast gesendet! 📢", "Broadcast sent! 📢")
    val spBroadcastHint get() = s("Nachricht an alle Nutzer...", "Message to all users...")
    val spBroadcastTitle get() = s("📢 Broadcast senden", "📢 Send Broadcast")
    val spUserBanned get() = s("Nutzer gebannt! 🚫", "User banned! 🚫")
    val spUserUnbanned get() = s("Nutzer entsperrt! ✅", "User unbanned! ✅")
    val spUserPromoted get() = s("Zum Admin befördert! 👑", "Promoted to Admin! 👑")
    val spUserDemoted get() = s("Zum User zurückgestuft! ⬇️", "Demoted to User! ⬇️")
    val spImpersonating get() = s("Eingeloggt als", "Logged in as")
    val spStopImpersonation get() = s("Zurück zum Admin", "Back to Admin")
    val spBanned get() = s("GESPERRT", "BANNED")
    val spSuperAdmin get() = s("SUPERADMIN", "SUPERADMIN")
    val spAdmin get() = s("ADMIN", "ADMIN")
    val spUser get() = s("USER", "USER")
    val spSystemInit get() = s("System initialisiert...", "System initialized...")
    val spConnectedDb get() = s("Verbunden mit Firestore [eu-west]", "Connected to Firestore [eu-west]")
    val spAdminSession get() = s("Admin-Sitzung gestartet.", "Admin session started.")
    val spMaintenanceScreen get() = s("🔧 Wartungsmodus\n\nDie App wird gerade gewartet.\nBitte versuche es später erneut.", "🔧 Maintenance Mode\n\nThe app is currently under maintenance.\nPlease try again later.")

    // ─── System Panel WG Management ───────────────────────────
    val spWgs get() = s("WGs", "WGs")
    val spWgMembers get() = s("Mitglieder", "Members")
    val spWgAdmin get() = s("Admin", "Admin")
    val spWgNoAdmin get() = s("Kein Admin", "No Admin")
    val spWgCode get() = s("Code", "Code")
    val spWgRent get() = s("Miete", "Rent")
    val spWgDelete get() = s("WG löschen", "Delete WG")
    val spWgDeleted get() = s("WG gelöscht! 🗑️", "WG deleted! 🗑️")
    val spWgDeleteConfirm get() = s("Bist du sicher? Alle Mitglieder werden entfernt und die WG wird endgültig gelöscht.", "Are you sure? All members will be removed and the WG will be permanently deleted.")
    val spWgDeleteTitle get() = s("⚠️ WG löschen?", "⚠️ Delete WG?")
    val spWgManageMembers get() = s("Mitglieder verwalten", "Manage Members")
    val spWgRemoveMember get() = s("Entfernen", "Remove")
    val spWgMemberRemoved get() = s("Mitglied entfernt!", "Member removed!")
    val spWgSetAdmin get() = s("Zum Admin machen", "Set as Admin")
    val spWgAdminSet get() = s("Admin zugewiesen! 👑", "Admin assigned! 👑")
    val spWgAddMember get() = s("Mitglied hinzufügen", "Add Member")
    val spWgMemberAdded get() = s("Mitglied hinzugefügt! ➕", "Member added! ➕")
    val spWgNoMembers get() = s("Keine Mitglieder", "No members")
    val spWgNoWgs get() = s("Keine WGs vorhanden", "No WGs found")
    val spWgAvailableUsers get() = s("Verfügbare Nutzer", "Available Users")
    val spWgNoAvailable get() = s("Keine Nutzer ohne WG", "No users without WG")

    // ─── Dashboard ────────────────────────────────────────────
    val dashYourWgLife get() = s("DEIN WG ALLTAG", "YOUR WG LIFE")
    val dashActive get() = s("AKTIV", "ACTIVE")
    val dashNow get() = s("JETZT", "NOW")
    val dashShopping get() = s("EINKAUFEN", "SHOPPING")
    val dashOpen get() = s("OFFEN", "OPEN")
    val dashAndMore get() = s("und mehr...", "and more...")
    val dashRelaxMode get() = s("Relax Modus", "Relax mode")
    val dashWeatherToday get() = s("HEUTE", "TODAY")
    val dashNoEvents get() = s("Keine Events geplant", "No upcoming events")
    val dashPlanSomething get() = s("Plane etwas Schönes!", "Plan something fun!")
    val dashBlackboardNote get() = s("PINNWAND NOTIZ", "BLACKBOARD NOTE")
    val dashMeals get() = s("Essen", "Meals")
    val dashStatus get() = s("Status", "Status")
    val dashCosts get() = s("Kosten", "Costs")
    val dashFame get() = s("Ruhm", "Fame")
    val dashGuest get() = s("Gast", "Guest")
    val dashSmart get() = s("Smart", "Smart")
    val dashRecent get() = s("Neueste:", "Recent:")
    val dashMsgSent get() = s("Nachricht gesendet! 💬", "Message sent! 💬")
    val dashNudged get() = s("angestupst! 👈", "nudged! 👈")
    val dashSendMsg get() = s("💬 Nachricht", "💬 Message")
    val dashNudge get() = s("🫳 Anstupsen", "🫳 Nudge")
    val dashChangeStatus get() = s("Status ändern", "Change Status")
    val dashHowAreYou get() = s("Wie geht's dir gerade?", "How are you feeling?")
    val dashStatusOnline get() = s("Ich bin da", "I'm here")
    val dashStatusAway get() = s("Unterwegs", "On the go")
    val dashStatusSleeping get() = s("Schlafe...", "Sleeping...")
    val dashStatusFocus get() = s("Nicht stören", "Do not disturb")
    val dashStatusParty get() = s("Party Modus", "Party Mode")
    val dashStatusShower get() = s("Duschen", "Showering")
    val dashStatusChanged get() = s("Status geändert:", "Status changed:")
    val dashShareWithWg get() = s("Teile etwas mit der WG", "Share something with the WG")
    val dashPresetQuiet get() = s("Bitte leise sein", "Please be quiet")
    val dashPresetGuests get() = s("Habe Besuch", "Have guests")
    val dashPresetFoodReady get() = s("Essen ist fertig!", "Food is ready!")
    val dashPresetCleaning get() = s("Putze gerade", "Currently cleaning")
    val dashPresetPackage get() = s("Paket kommt", "Package coming")
    val dashWlanPassword get() = s("WLAN Passwort: SuperSecret123", "WiFi Password: SuperSecret123")
    val dashDismiss get() = s("Schließen", "Dismiss")

    // ─── Analytics ────────────────────────────────────────────
    val period7d get() = s("7T", "7D")
    val period30d get() = s("30T", "30D")
    val periodYear get() = s("Jahr", "Year")
    val tasksTabEmoji get() = s("✨ Aufgaben", "✨ Tasks")
    val financesTabEmoji get() = s("💵 Finanzen", "💵 Finances")
    val exportReport get() = s("Bericht exportieren", "Export Report")
    val reportExported get() = s("Bericht exportiert! 📤", "Report exported! 📤")
    val vsLastMonth get() = s("+12% vs letztem Monat", "+12% vs last month")
    val topPerformer get() = s("TOP PERFORMER", "TOP PERFORMER")
    val noData get() = s("Keine Daten", "No data")
    val totalSpending get() = s("GESAMTAUSGABEN", "TOTAL SPENDING")
    val drinks get() = s("Getränke", "Drinks")
    val householdCat get() = s("Haushalt", "Household")
    val otherCat get() = s("Sonstiges", "Other")
    val totalUpper get() = s("GESAMT", "TOTAL")
    val tasksSuffix get() = s("Aufgaben", "Tasks")

    // ─── Crew ─────────────────────────────────────────────────
    val wgCrew get() = s("WG Crew", "WG Crew")
    val addMember get() = s("Mitglied hinzufügen", "Add member")
    val conflictResolver get() = s("✨ KONFLIKTLÖSER ✨", "✨ CONFLICT RESOLVER ✨")
    val wasChosen get() = s("wurde ausgewählt! 🎯", "was chosen! 🎯")
    val whoDoesIt get() = s("Wer macht's? 🎲", "Who does it? 🎲")
    val membersLabel get() = s("MITGLIEDER", "MEMBERS")
    val kickLabel get() = s("Kicken", "Kick")
    val kickConfirm get() = s("kicken?", "kick?")
    val willBeRemoved get() = s("Wird aus der WG entfernt.", "Will be removed from the WG.")
    val removedMsg get() = s("entfernt", "removed")
    val newMember get() = s("Neues Mitglied", "New Member")
    val enterName get() = s("Name eingeben...", "Enter name...")
    val addedToWg get() = s("hinzugefügt! 🎉", "added! 🎉")

    // ─── Calendar (extended) ──────────────────────────────────
    val wgCalendar get() = s("WG Kalender", "WG Calendar")
    val oldEventsDeleted get() = s("Vergangene Events gelöscht! 🗑️", "Past events deleted! 🗑️")
    val noOldEvents get() = s("Keine alten Events", "No old events")
    val allFilter get() = s("Alle", "All")
    val noEvents get() = s("Keine Events.", "No events.")
    val todayLabel get() = s("HEUTE", "TODAY")
    val createdByLabel2 get() = s("ERSTELLT VON", "CREATED BY")
    val byLabel get() = s("von", "by")
    val detailsLabel get() = s("Details", "Details")
    val newEventLabel get() = s("Neues Event", "New Event")
    val exportIcs get() = s("⬇ Exportieren (.ics)", "⬇ Export (.ics)")
    val editBtnIcon get() = s("✏ Bearbeiten", "✏ Edit")
    val eventPlaceholder get() = s("Event Titel", "Event Title")
    val eventAdded get() = s("Event hinzugefügt! 📅", "Event added! 📅")
    val eventDeleted get() = s("Event gelöscht 🗑️", "Event deleted 🗑️")
    val eventUpdated get() = s("Event aktualisiert! ✅", "Event updated! ✅")
    val monthsShortDe get() = listOf("JAN.", "FEB.", "MÄR.", "APR.", "MAI", "JUN.", "JUL.", "AUG.", "SEP.", "OKT.", "NOV.", "DEZ.")
    val monthsShortEn get() = listOf("JAN.", "FEB.", "MAR.", "APR.", "MAY", "JUN.", "JUL.", "AUG.", "SEP.", "OCT.", "NOV.", "DEC.")
    val monthsShort get() = if (currentLanguage == AppLanguage.DE) monthsShortDe else monthsShortEn

    // ─── Cleaning (extended) ──────────────────────────────────
    val cleaningPlan get() = s("Putzplan", "Cleaning Plan")
    val weekNum get() = s("Woche", "Week")
    val tasksRotated get() = s("Aufgaben rotiert! 🔄", "Tasks rotated! 🔄")
    val memberNudged get() = s("angestupst! 📢", "nudged! 📢")
    val strikeGiven get() = s("Strike vergeben! ⚠️", "Strike given! ⚠️")
    val xpEarned get() = s("+10 XP! ✅", "+10 XP! ✅")
    val taskReset get() = s("Aufgabe zurückgesetzt", "Task reset")
    val taskCreated get() = s("Aufgabe erstellt! ✅", "Task created! ✅")
    val newTaskPlaceholder get() = s("Neue Aufgabe...", "New task...")
    val whoPlaceholder get() = s("Wer?", "Who?")
    val monthsCleanDe get() = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
    val monthsCleanEn get() = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthsClean get() = if (currentLanguage == AppLanguage.DE) monthsCleanDe else monthsCleanEn

    // ─── Blackboard (extended) ────────────────────────────────
    val pinnwand get() = s("Pinnwand", "Pinboard")
    val entriesCount get() = s("EINTRÄGE", "ENTRIES")
    val noEntries get() = s("Keine Einträge.", "No entries.")
    val newEntryLabel get() = s("Neuer Eintrag", "New Entry")
    val typeLabel get() = s("TYP", "TYPE")
    val complaintUpper get() = s("BESCHWERDE", "COMPLAINT")
    val kudosUpper get() = s("LOB", "KUDOS")
    val pollUpper get() = s("UMFRAGE", "POLL")
    val writeMessage get() = s("Nachricht schreiben…", "Write message…")
    val commaSeparated get() = s("Optionen (Komma getrennt)", "Options (comma separated)")
    val postBtn get() = s("Posten", "Post")
    val markResolved get() = s("✓ Als gelöst markieren", "✓ Mark as resolved")
    val resolvedLabel get() = s("GELÖST ✓", "RESOLVED ✓")
    val entryCreated get() = s("Eintrag erstellt! 📋", "Entry created! 📋")
    val votedFor get() = s("Abgestimmt für", "Voted for")
    val markedResolved get() = s("Als gelöst markiert ✅", "Marked as resolved ✅")

    // ─── Vault (extended) ─────────────────────────────────────
    val wgVault get() = s("WG Tresor", "WG Vault")
    val encryptedInfo get() = s("Diese Infos sind verschlüsselt und nur für WG-Mitglieder sichtbar.", "This info is encrypted and only visible to WG members.")
    val noEntriesYet get() = s("Noch keine Einträge.", "No entries yet.")
    val titleFieldLabel get() = s("TITEL", "TITLE")
    val chooseIconLabel get() = s("ICON WÄHLEN", "CHOOSE ICON")
    val categoryFieldLabel get() = s("KATEGORIE", "CATEGORY")
    val contentFieldLabel get() = s("INHALT", "CONTENT")
    val hiddenLabel get() = s("Versteckt", "Hidden")
    val newEntryVault get() = s("Neuer Eintrag", "New Entry")
    val deletedItem get() = s("Gelöscht 🗑️", "Deleted 🗑️")
    val qrShown get() = s("QR Code angezeigt! 📱", "QR Code shown! 📱")
    val copiedClipboard get() = s("In Zwischenablage kopiert! 📋", "Copied to clipboard! 📋")
    val entryAddedVault get() = s("Eintrag hinzugefügt! 🔐", "Entry added! 🔐")

    // ─── Meal Planner (extended) ──────────────────────────────
    val mealPlannerTitle get() = s("ESSENSPLANER", "MEAL PLANNER")
    val cooksAssigned get() = s("Köche zugewiesen! 👨‍🍳", "Cooks assigned! 👨‍🍳")
    val autoAssign get() = s("Auto Zuweisung", "Auto assign")
    val plannedLabel get() = s("GEPLANT", "PLANNED")
    val ingredientsAddedShopping get() = s("Zutaten hinzugefügt! 🛒", "Ingredients added! 🛒")
    val cookLabelMeal get() = s("👨‍🍳 Koch:", "👨‍🍳 Cook:")
    val notAssigned get() = s("Nicht zugewiesen", "Not assigned")
    val minUnit get() = s("Min", "Min")
    val ingredientsLabel get() = s("Zutaten:", "Ingredients:")
    val addToShoppingBtn get() = s("🛒 Einkaufen", "🛒 Add to shopping")
    val dayCleared get() = s("Tag geleert", "Day cleared")
    val removeLabel get() = s("Entfernen", "Remove")
    val recipeCreated get() = s("Rezept erstellt! 📖", "Recipe created! 📖")
    val planBtn get() = s("Plan", "Plan")
    val planAction get() = s("Planen", "Plan")
    val filterAll get() = s("Alle", "All")
    val filterQuick get() = s("Schnell (<30m)", "Quick (<30m)")
    val filterEasy get() = s("Einfach", "Easy")
    val filterElaborate get() = s("Aufwendig", "Elaborate")
    val chooseDish get() = s("Gericht wählen", "Choose dish")
    val newRecipe get() = s("Neues Rezept", "New Recipe")
    val createRecipe get() = s("Rezept erstellen", "Create Recipe")
    val dishName get() = s("Name des Gerichts", "Dish name")
    val difficultyEasy get() = s("Leicht", "Easy")
    val difficultyMedium get() = s("Mittel", "Medium")
    val difficultyHard get() = s("Schwer", "Hard")
    val ingredientsComma get() = s("Zutaten (durch Komma getrennt)", "Ingredients (comma separated)")

    // ─── Profile (extended) ───────────────────────────────────
    val badgeEarlyBird get() = s("Frühaufsteher", "Early Bird")
    val badgeEarlyBirdDesc get() = s("5 Aufgaben vor 9 Uhr erledigt", "Complete 5 tasks before 9 AM")
    val badgeCleanFreak get() = s("Putzfee", "Clean Freak")
    val badgeCleanFreakDesc get() = s("20 Putzaufgaben erledigt", "Complete 20 cleaning tasks")
    val badgeMoneyMaker get() = s("Sparfuchs", "Money Maker")
    val badgeMoneyMakerDesc get() = s("Budget 3 Monate eingehalten", "Stay under budget for 3 months")
    val badgePartyAnimal get() = s("Partylöwe", "Party Animal")
    val badgePartyAnimalDesc get() = s("10 WG-Events organisiert", "Host 10 WG events")
    val badgeTopChef get() = s("Sternekoch", "Top Chef")
    val badgeTopChefDesc get() = s("30 Mahlzeiten geplant", "Plan 30 meals")
    val badgeGhost get() = s("Geist", "Ghost")
    val badgeGhostDesc get() = s("7 Tage nicht eingeloggt", "Don't log in for 7 days")
    val defaultBio get() = s("WG-Leben ist das Beste! ✌️", "Living the WG life! ✌️")
    val settingsShort get() = s("Einst.", "Sett.")
    val nameFieldLabel get() = s("Name", "Name")
    val bioFieldLabel get() = s("Bio", "Bio")
    val passwordChangedFull get() = s("Passwort geändert 🔒", "Password changed 🔒")
    val unlockedUpper get() = s("FREIGESCHALTET", "UNLOCKED")
    val lockedUpper get() = s("GESPERRT", "LOCKED")
    val emailDescription get() = s("Dies ist deine registrierte Adresse.", "This is your registered email address.")
    val memberSinceDesc get() = s("Du bist der WG im Oktober 2023 beigetreten.", "You joined the WG in October 2023.")
    val xpToNextLevel get() = s("Du brauchst noch", "You need")
    val xpToNextLevelEnd get() = s("XP für das nächste Level!", "XP to reach the next level!")
    val top10Pct get() = s("Top 10%", "Top 10%")
    val avgLabel get() = s("Durchschn.", "Avg")
    val daysUnit get() = s("Tage", "Days")
    val onFire get() = s("Am Brennen!", "On Fire!")
    val fastLabel get() = s("Schnell", "Fast")
    val approxMins get() = s("~12 Min", "~12 mins")
    val settingsUpper get() = s("EINSTELLUNGEN", "SETTINGS")
    val securityUpper get() = s("SICHERHEIT", "SECURITY")
    val legalUpper get() = s("RECHTLICHES & HILFE", "LEGAL & HELP")
    val accountUpper get() = s("KONTO", "ACCOUNT")
    val twoFaActivated get() = s("2FA aktiviert ✅", "2FA enabled ✅")
    val twoFaDeactivated get() = s("2FA deaktiviert", "2FA disabled")
    val signOutBtn get() = s("↪ Abmelden", "↪ Sign Out")
    val appVersion get() = s("WG Manager v1.0.5 • Build 2407", "WG Manager v1.0.5 • Build 2407")
    val youSuffix get() = s(" (du)", " (you)")
    val activeSuffix get() = s("aktiv", "active")
    val activeUpper get() = s("AKTIV", "ACTIVE")

    // ─── Shopping Extras ──────────────────────────────────────
    val quickChipBeer get() = s("🍺 Bierkasten", "🍺 Beer Crate")
    val quickChipCleaning get() = s("🧹 Putzmittel", "🧹 Cleaning Supplies")
    val quickChipSnacks get() = s("🍫 Snacks", "🍫 Snacks")

    // ─── Dashboard Calendar Countdown ─────────────────────────
    val daysUntil get() = s("Tage", "Days")
    val dayUntil get() = s("Tag", "Day")
    val asNext get() = s("ALS NÄCHSTES", "COMING UP")
    val today get() = s("HEUTE", "TODAY")

    // ─── Calendar Screen ──────────────────────────────────────
    val repeatLabel get() = s("Wiederholung", "Repeat")
    val noRepeat get() = s("Keine", "None")
    val weeklyRepeat get() = s("Wöchentlich", "Weekly")
    val monthlyRepeat get() = s("Monatlich", "Monthly")
    val weeklyMonthRepeat get() = s("Wöchentlich (1 Monat)", "Weekly (1 Month)")

    // ─── Meal Planner Dishes CRUD ─────────────────────────────
    val dishesTab get() = s("Gerichte", "Dishes")
    val weekPlanTab get() = s("Wochenplan", "Week Plan")
    val noDishesYet get() = s("Noch keine Gerichte angelegt", "No dishes created yet")
    val editDish get() = s("Gericht bearbeiten", "Edit Dish")
    val deleteDish get() = s("Gericht löschen", "Delete Dish")
    val dishDeleted get() = s("Gericht gelöscht!", "Dish deleted!")
    val dishUpdated get() = s("Gericht aktualisiert!", "Dish updated!")

    // ─── Vault Admin Edit ─────────────────────────────────────
    val editEntryVault get() = s("Eintrag bearbeiten", "Edit Entry")
    val entryUpdatedVault get() = s("Eintrag aktualisiert!", "Entry updated!")

    // ─── Recurring Costs Edit ─────────────────────────────────
    val editCost get() = s("Kosten bearbeiten", "Edit Cost")
    val costUpdated get() = s("Kosten aktualisiert!", "Cost updated!")

    // ─── Export / PDF ─────────────────────────────────────────
    val exportPdf get() = s("Als PDF exportieren", "Export as PDF")
    val printLabel get() = s("Drucken", "Print")
    val exportedSuccess get() = s("Erfolgreich exportiert!", "Exported successfully!")
    val exportCalendar get() = s("Kalender exportieren", "Export Calendar")
    val exportShoppingList get() = s("Einkaufsliste exportieren", "Export Shopping List")
    val exportCostReport get() = s("Kostenübersicht exportieren", "Export Cost Report")

    // ─── Shopping Budget Admin ────────────────────────────────
    val setBudget get() = s("Budget festlegen", "Set Budget")
    val editItem get() = s("Artikel bearbeiten", "Edit Item")
    val itemUpdated get() = s("Artikel aktualisiert!", "Item updated!")
    val budgetLabel get() = s("Monatsbudget (€)", "Monthly Budget (€)")

    // ─── Equipment / Amenities ────────────────────────────────
    val amenitiesToggled get() = s("Ausstattung aktualisiert!", "Amenities updated!")

    // ─── Crew Extras ──────────────────────────────────────────
    val top3Label get() = s("TOP 3", "TOP 3")
    val xpLabel get() = s("XP", "XP")
    val aiReferee get() = s("AI SCHIEDSRICHTER", "AI REFEREE")
    val aiRefereeDesc get() = s("Uneinigkeit? Lass die KI fair auf Basis von Punkten und Verlauf entscheiden.", "Disagreement? Let the AI decide fairly based on points and history.")
    val judgeUs get() = s("⚡ Judge Us", "⚡ Judge Us")
    val aiJudging get() = s("KI analysiert...", "AI analyzing...")
    val aiVerdict get() = s("KI-Urteil", "AI Verdict")

    // Helper function to get string based on current language
    private fun s(de: String, en: String): String {
        return when (currentLanguage) {
            AppLanguage.DE -> de
            AppLanguage.EN -> en
        }
    }

    var currentLanguage: AppLanguage = AppLanguage.DE
        private set

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
    }

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == AppLanguage.DE) AppLanguage.EN else AppLanguage.DE
    }
}
