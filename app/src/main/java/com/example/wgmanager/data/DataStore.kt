package com.example.wgmanager.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════
enum class UserRole { USER, ADMIN, SUPER_ADMIN }
enum class UserStatus(val emoji: String, val label: String) {
    ONLINE("🟢", "Online"), SLEEPING("😴", "Sleeping"), FOCUS("🎯", "Focus"),
    PARTY("🎉", "Party"), SHOWER("🚿", "Shower"), AWAY("👻", "Away")
}
enum class ShoppingStatus { PENDING, BOUGHT }
enum class TicketType { COMPLAINT, KUDOS, POLL }
enum class EventType { PARTY, QUIET, VISIT, GENERAL }
enum class VaultType { WIFI, PHONE, IBAN, TEXT, CODE }
enum class ThemeColor { INDIGO, EMERALD, ROSE, AMBER, SKY }
enum class RecurringFrequency { MONTHLY, WEEKLY }
enum class OnboardingStepType { READ_RULES, ADD_IBAN, PICK_CLEANING_DAY, SET_AVATAR, INTRODUCE_YOURSELF }
enum class PantryStatus { FULL, LOW, EMPTY }

// ═══════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════
data class WG(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var address: String = "",
    val joinCode: String = generateCode(),
    var rentPrice: Int = 0,
    var publicDescription: String = "",
    var wgRules: String = "",
    var monthlyBudget: Double = 400.0,
    var amenities: MutableList<String> = mutableListOf("wifi", "washer")
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    var wgId: String = "",
    var name: String,
    var email: String,
    var password: String = "1234",
    var role: UserRole = UserRole.USER,
    var hasWG: Boolean = false,
    var points: Int = 0,
    var status: UserStatus = UserStatus.ONLINE,
    var isTwoFactorEnabled: Boolean = false,
    var bio: String = "",
    var avatarEmoji: String = "👤",
    var themeColor: ThemeColor = ThemeColor.INDIGO,
    var isDarkMode: Boolean = true,
    var language: String = "DE",
    var notificationsEnabled: Boolean = true,
    var onboardingCompleted: Boolean = false,
    var onboardingSteps: MutableList<OnboardingItem> = mutableListOf(),
    var monthlyBadge: String = "",  // crown/clown badge
    var isBanned: Boolean = false
) {
    val levelTitle: String get() = when {
        points >= 500 -> "🏆 Legende"
        points >= 300 -> "⭐ Meister"
        points >= 150 -> "🔥 Profi"
        points >= 50 -> "💪 Aktiv"
        else -> "🌱 Neuling"
    }
}

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String = "📦",
    var price: Double = 0.0,
    var addedBy: String = "",
    var boughtBy: String = "",
    var status: ShoppingStatus = ShoppingStatus.PENDING,
    var date: String = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var assignedTo: String,
    var completed: Boolean = false,
    var streak: Int = 0
)

data class Ticket(
    val id: String = UUID.randomUUID().toString(),
    var type: TicketType,
    var text: String,
    var author: String,
    var isSolved: Boolean = false,
    var pollOptions: List<String> = emptyList(),
    var pollVotes: MutableMap<String, String> = mutableMapOf(), // userName -> option
    var date: String = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())
)

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var date: String, // dd.MM.yyyy
    var type: EventType = EventType.GENERAL,
    var emoji: String = "📅",
    var createdBy: String = "",
    var isRecurring: Boolean = false
)

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String = "🍽️",
    var difficulty: String = "Easy",
    var timeMinutes: Int = 30,
    var ingredients: List<String> = emptyList()
)

data class MealPlanDay(
    var day: String, // "Mon", "Tue" etc.
    var recipeId: String? = null,
    var cook: String = ""
)

data class VaultItem(
    val id: String = UUID.randomUUID().toString(),
    var label: String,
    var value: String,
    var type: VaultType = VaultType.TEXT,
    var customIcon: String = "🔑",
    var isSecure: Boolean = false
)

data class RewardItem(
    val id: String = UUID.randomUUID().toString(),
    var emoji: String,
    var title: String,
    var cost: Int,
    var description: String
)

data class JoinRequest(
    val id: String = UUID.randomUUID().toString(),
    var userName: String,
    var userEmail: String,
    var userId: String = "",
    var message: String = "",
    var wgId: String = "",
    var date: String = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())
)

data class RecurringCost(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String = "💸",
    var totalAmount: Double,
    var frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    var paidBy: String = "",  // admin or person who pays the bill
    var isActive: Boolean = true
)

data class GuestPass(
    val id: String = UUID.randomUUID().toString(),
    var guestName: String,
    var createdBy: String,
    var wgId: String = "",
    var accessCode: String = generateGuestCode(),
    var wifiPassword: String = "",
    var isActive: Boolean = true,
    var createdDate: String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date())
) {
    companion object {
        fun generateGuestCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            return "G-" + (1..6).map { chars.random() }.joinToString("")
        }
    }
}

data class SmartScene(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String,
    var description: String,
    var notificationText: String,
    var isActive: Boolean = false
)

data class OnboardingItem(
    val type: OnboardingStepType,
    var completed: Boolean = false
)

data class PantryItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String = "📦",
    var status: PantryStatus = PantryStatus.FULL,
    var updatedBy: String = "",
    var date: String = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())
)

// ═══════════════════════════════════════════════════════════════
// GLOBAL STATE (singleton — backed by Firebase via FirebaseSync)
// ═══════════════════════════════════════════════════════════════
object DataStore {
    // Current session
    var currentUser: User? = null
    var currentWG: WG? = null
    var originalSuperAdmin: User? = null  // for impersonation
    var maintenanceMode: Boolean = false
    var broadcastMessage: String = ""
    val systemLogs = mutableListOf<String>()

    // All data lists
    val wgs = mutableListOf<WG>()
    val users = mutableListOf<User>()
    val shoppingItems = mutableListOf<ShoppingItem>()
    val tasks = mutableListOf<Task>()
    val tickets = mutableListOf<Ticket>()
    val events = mutableListOf<CalendarEvent>()
    val recipes = mutableListOf<Recipe>()
    val mealPlan = mutableListOf<MealPlanDay>()
    val vaultItems = mutableListOf<VaultItem>()
    val rewards = mutableListOf<RewardItem>()
    val pendingRequests = mutableListOf<JoinRequest>()
    val recurringCosts = mutableListOf<RecurringCost>()
    val guestPasses = mutableListOf<GuestPass>()
    val smartScenes = mutableListOf<SmartScene>()
    val pantryItems = mutableListOf<PantryItem>()

    // ── Firebase-aware initialisation ───────────────────────
    fun initFromFirebase(onReady: () -> Unit = {}) {
        FirebaseSync.loadAll { loaded ->
            if (!loaded) {
                // First launch or offline — seed with mock data
                if (users.isEmpty()) initMockData()
                FirebaseSync.pushAll()
            }
            onReady()
        }
    }

    // ── Init mock data ──────────────────────────────────────
    fun initMockData() {
        if (users.isNotEmpty()) return // Already initialized

        // WGs
        val wg1 = WG(name = "Sunny Flat", address = "Berliner Str. 42, Berlin", joinCode = "SUNNY", rentPrice = 450, publicDescription = "A sunny flat in the heart of Berlin with great vibes and a big balcony.",
            wgRules = "1. Nachtruhe ab 22 Uhr\n2. Küche nach Benutzung aufräumen\n3. Müll nach Putzplan rausbringen\n4. Gäste vorher ankündigen\n5. Gemeinsame Ausgaben fair teilen",
            monthlyBudget = 400.0, amenities = mutableListOf("wifi", "washer", "balcony", "dishwasher"))
        val wg2 = WG(name = "Mountain View", address = "Alpenstr. 7, München", joinCode = "MOUNT", rentPrice = 520, publicDescription = "Cozy mountain flat with stunning Alpine views and a shared garden.",
            wgRules = "1. Schuhe ausziehen\n2. Ruhezeiten respektieren\n3. Gemeinschaftsbereiche sauber halten",
            monthlyBudget = 500.0, amenities = mutableListOf("wifi", "washer", "parking", "garden"))
        wgs.addAll(listOf(wg1, wg2))

        // Users (existing users have completed onboarding)
        val max = User(name = "Max", email = "max@wg.com", role = UserRole.USER, hasWG = true, points = 120, wgId = wg1.id, avatarEmoji = "😎", onboardingCompleted = true)
        val anna = User(name = "Anna", email = "admin@wg.com", role = UserRole.ADMIN, hasWG = true, points = 450, wgId = wg1.id, avatarEmoji = "👩‍💼", onboardingCompleted = true)
        val tom = User(name = "Tom", email = "tom@wg.com", role = UserRole.USER, hasWG = true, points = 85, wgId = wg1.id, status = UserStatus.FOCUS, avatarEmoji = "🧑‍💻", onboardingCompleted = true)
        val lisa = User(name = "Lisa", email = "lisa@wg.com", role = UserRole.ADMIN, hasWG = true, points = 210, wgId = wg2.id, avatarEmoji = "👩‍🎨", onboardingCompleted = true)
        val john = User(name = "John", email = "john@wg.com", role = UserRole.USER, hasWG = true, points = 40, wgId = wg2.id, avatarEmoji = "🧔", onboardingCompleted = true)
        val guest = User(name = "Guest", email = "guest@wg.com", role = UserRole.USER, hasWG = false, points = 0, avatarEmoji = "👻", onboardingCompleted = false)
        val superDev = User(name = "Super Dev", email = "super@wg.com", role = UserRole.SUPER_ADMIN, hasWG = true, points = 9999, wgId = wg1.id, avatarEmoji = "🦸", onboardingCompleted = true)
        val newGuy = User(name = "New Guy", email = "new@wg.com", role = UserRole.USER, hasWG = false, points = 0, avatarEmoji = "🐣", onboardingCompleted = false)
        users.addAll(listOf(anna, max, tom, lisa, john, guest, superDev, newGuy))

        // Shopping
        shoppingItems.addAll(listOf(
            ShoppingItem(name = "Milk", emoji = "🥛", price = 1.20, addedBy = "Anna"),
            ShoppingItem(name = "Toilet Paper", emoji = "🧱", price = 4.50, addedBy = "Max"),
            ShoppingItem(name = "Pasta", emoji = "🍝", price = 0.99, addedBy = "Tom"),
            ShoppingItem(name = "Beer", emoji = "🍺", price = 3.50, addedBy = "Max", boughtBy = "Max", status = ShoppingStatus.BOUGHT),
        ))

        // Tasks
        tasks.addAll(listOf(
            Task(title = "Clean Kitchen", assignedTo = "Max"),
            Task(title = "Take out Trash", assignedTo = "Anna"),
            Task(title = "Vacuum Living Room", assignedTo = "Tom"),
            Task(title = "Clean Bathroom", assignedTo = "Max"),
        ))

        // Tickets
        tickets.addAll(listOf(
            Ticket(type = TicketType.COMPLAINT, text = "Music too loud at night!", author = "Max"),
            Ticket(type = TicketType.KUDOS, text = "Great dinner last night! 🍝", author = "Anna"),
            Ticket(type = TicketType.POLL, text = "Movie night - which day?", author = "Tom",
                pollOptions = listOf("Friday", "Saturday", "Sunday")),
        ))

        // Calendar events
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        val cal = Calendar.getInstance()
        events.addAll(listOf(
            CalendarEvent(title = "WG Party 🎉", date = run { cal.add(Calendar.DAY_OF_MONTH, 3); sdf.format(cal.time) }, type = EventType.PARTY, emoji = "🎉", createdBy = "Anna"),
            CalendarEvent(title = "Quiet Hours", date = run { cal.add(Calendar.DAY_OF_MONTH, 5); sdf.format(cal.time) }, type = EventType.QUIET, emoji = "🤫", createdBy = "Tom"),
            CalendarEvent(title = "Parents Visit", date = run { cal.add(Calendar.DAY_OF_MONTH, 2); sdf.format(cal.time) }, type = EventType.VISIT, emoji = "👨‍👩‍👦", createdBy = "Max"),
        ))

        // Recipes
        recipes.addAll(listOf(
            Recipe(name = "Spaghetti Bolognese", emoji = "🍝", difficulty = "Easy", timeMinutes = 30, ingredients = listOf("Spaghetti", "Ground Beef", "Tomato Sauce", "Onion", "Garlic")),
            Recipe(name = "Chicken Curry", emoji = "🍛", difficulty = "Medium", timeMinutes = 45, ingredients = listOf("Chicken", "Curry Paste", "Coconut Milk", "Rice", "Peppers")),
            Recipe(name = "Caesar Salad", emoji = "🥗", difficulty = "Easy", timeMinutes = 15, ingredients = listOf("Lettuce", "Croutons", "Parmesan", "Caesar Dressing")),
            Recipe(name = "Pizza Margherita", emoji = "🍕", difficulty = "Medium", timeMinutes = 40, ingredients = listOf("Pizza Dough", "Mozzarella", "Tomato Sauce", "Basil")),
            Recipe(name = "Pancakes", emoji = "🥞", difficulty = "Easy", timeMinutes = 20, ingredients = listOf("Flour", "Eggs", "Milk", "Sugar", "Butter")),
        ))

        // Meal plan
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        mealPlan.clear()
        mealPlan.addAll(days.mapIndexed { i, day ->
            if (i < 3) MealPlanDay(day = day, recipeId = recipes[i].id, cook = users[i % 3].name)
            else MealPlanDay(day = day)
        })

        // Vault
        vaultItems.addAll(listOf(
            VaultItem(label = "WiFi Password", value = "SunnyFlat2024!", type = VaultType.WIFI, customIcon = "📶", isSecure = true),
            VaultItem(label = "Landlord Phone", value = "+49 170 1234567", type = VaultType.PHONE, customIcon = "📞"),
            VaultItem(label = "WG IBAN", value = "DE89 3704 0044 0532 0130 00", type = VaultType.IBAN, customIcon = "🏦", isSecure = true),
            VaultItem(label = "Front Door Code", value = "4523#", type = VaultType.CODE, customIcon = "🚪", isSecure = true),
        ))

        // Rewards
        rewards.addAll(listOf(
            RewardItem(emoji = "🎬", title = "Filmabend Wahl", cost = 30, description = "Nächsten Film für den Filmabend auswählen"),
            RewardItem(emoji = "🛋️", title = "Sofa Priorität", cost = 50, description = "Bester Platz auf dem Sofa für eine Woche"),
            RewardItem(emoji = "🍕", title = "Gratis Pizza", cost = 80, description = "Die WG kauft dir eine Pizza"),
            RewardItem(emoji = "🧹", title = "Putzen überspringen", cost = 100, description = "Eine Putzrunde überspringen"),
            RewardItem(emoji = "👑", title = "König/in der WG", cost = 200, description = "Krone-Emoji für eine Woche"),
        ))

        // Recurring costs
        recurringCosts.addAll(listOf(
            RecurringCost(name = "Internet", emoji = "📶", totalAmount = 39.99, paidBy = anna.name),
            RecurringCost(name = "Netflix", emoji = "🎬", totalAmount = 15.99, paidBy = max.name),
            RecurringCost(name = "Strom", emoji = "⚡", totalAmount = 85.00, paidBy = anna.name),
            RecurringCost(name = "GEZ", emoji = "📺", totalAmount = 18.36, paidBy = tom.name),
        ))

        // Smart scenes
        smartScenes.addAll(listOf(
            SmartScene(name = "Movie Night", emoji = "🎬", description = "Dimmed lights, TV on, popcorn time", notificationText = "🎬 Movie Night activated! Grab your snacks!"),
            SmartScene(name = "Party Mode", emoji = "🎉", description = "Lights colorful, music loud, vibes high", notificationText = "🎉 Party Mode ON! Let's gooo!"),
            SmartScene(name = "Study Time", emoji = "📚", description = "Quiet mode, focus lights, do not disturb", notificationText = "📚 Study Time — please keep it quiet!"),
            SmartScene(name = "Good Night", emoji = "🌙", description = "All lights off, doors locked, quiet hours", notificationText = "🌙 Good Night mode — sweet dreams!"),
            SmartScene(name = "Morning Routine", emoji = "☀️", description = "Coffee machine on, lights warm, music soft", notificationText = "☀️ Good morning! Coffee is brewing!"),
        ))

        // Pantry items
        pantryItems.addAll(listOf(
            PantryItem(name = "Milk", emoji = "🥛", status = PantryStatus.LOW, updatedBy = "Anna"),
            PantryItem(name = "Rice", emoji = "🍚", status = PantryStatus.FULL, updatedBy = "Max"),
            PantryItem(name = "Pasta", emoji = "🍝", status = PantryStatus.FULL, updatedBy = "Tom"),
            PantryItem(name = "Coffee", emoji = "☕", status = PantryStatus.EMPTY, updatedBy = "Anna"),
            PantryItem(name = "Butter", emoji = "🧈", status = PantryStatus.LOW, updatedBy = "Max"),
            PantryItem(name = "Bread", emoji = "🍞", status = PantryStatus.EMPTY, updatedBy = "Tom"),
        ))
    }

    // ── Authentication ──────────────────────────────────────
    fun login(email: String, password: String): User? {
        val user = users.find { it.email.equals(email, true) && it.password == password }
        if (user != null) {
            currentUser = user
            // Existing users with a WG should have onboarding marked as complete
            if (user.hasWG && user.role != UserRole.USER) {
                // Admins and super admins always skip onboarding
                if (!user.onboardingCompleted) {
                    user.onboardingCompleted = true
                    FirebaseSync.pushUser(user)
                }
            }
        }
        return user
    }

    fun register(name: String, email: String, password: String): User? {
        if (users.any { it.email.equals(email, true) }) return null
        val user = User(name = name, email = email, password = password)
        users.add(user)
        currentUser = user
        FirebaseSync.pushUser(user)
        return user
    }

    fun logout() { currentUser = null; currentWG = null }

    // ── WG helpers ──────────────────────────────────────────
    fun getWGMembers(): List<User> {
        val wgId = currentUser?.wgId ?: return emptyList()
        return users.filter { it.wgId == wgId && it.hasWG }
    }

    fun joinWGByCode(code: String): Boolean {
        val wg = wgs.find { it.joinCode.equals(code, true) } ?: return false
        currentUser?.let { u -> u.wgId = wg.id; u.hasWG = true; currentWG = wg; FirebaseSync.pushUser(u) }
        return true
    }

    fun createWG(name: String, address: String): WG {
        val wg = WG(name = name, address = address)
        wgs.add(wg)
        currentUser?.let { u -> u.wgId = wg.id; u.hasWG = true; FirebaseSync.pushUser(u) }
        currentWG = wg
        FirebaseSync.pushWG(wg)
        return wg
    }

    // ── Shopping helpers ────────────────────────────────────
    fun addShoppingItem(name: String, price: Double) {
        val item = ShoppingItem(name = name, price = price, addedBy = currentUser?.name ?: "")
        shoppingItems.add(item)
        FirebaseSync.pushShoppingItem(item)
    }

    fun buyItem(item: ShoppingItem) {
        item.status = ShoppingStatus.BOUGHT
        item.boughtBy = currentUser?.name ?: ""
        FirebaseSync.pushShoppingItem(item)
    }

    fun removeShoppingItem(item: ShoppingItem) {
        shoppingItems.remove(item)
        FirebaseSync.removeShoppingItem(item.id)
    }

    // Quick restock suggestions
    val quickRestockItems = listOf("Milk", "Bread", "Eggs", "Butter", "Water", "Coffee", "Bananas", "Cheese")

    // Balance calculation — only count BOUGHT items
    fun calculateBalances(): Map<String, Double> {
        val members = getWGMembers()
        if (members.isEmpty()) return emptyMap()
        val boughtItems = shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }
        val totalSpent = boughtItems.sumOf { it.price }
        val perPerson = totalSpent / members.size
        return members.associate { user ->
            val paid = boughtItems.filter { it.boughtBy == user.name }.sumOf { it.price }
            user.name to (paid - perPerson)
        }
    }

    /**
     * Settle debt with a specific creditor.
     * Removes all BOUGHT items from the shopping list (clearing the balance slate).
     * In a real app this would record a "settlement" transaction — here we clear bought items
     * so all balances reset to zero.
     */
    fun settleAllDebts() {
        val boughtItems = shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }.toList()
        boughtItems.forEach { item ->
            shoppingItems.remove(item)
            FirebaseSync.removeShoppingItem(item.id)
        }
    }

    /**
     * Settle debt with a single creditor by removing only their bought items.
     * This effectively zeroes out that creditor's positive balance.
     */
    fun settleDebtWith(creditorName: String) {
        val creditorItems = shoppingItems.filter {
            it.status == ShoppingStatus.BOUGHT && it.boughtBy == creditorName
        }.toList()
        creditorItems.forEach { item ->
            shoppingItems.remove(item)
            FirebaseSync.removeShoppingItem(item.id)
        }
    }

    // ── Tasks helpers ───────────────────────────────────────
    fun toggleTask(task: Task) {
        task.completed = !task.completed
        val assignee = users.find { it.name == task.assignedTo }
        if (task.completed) {
            assignee?.let { it.points += 10 }
            task.streak++
        } else {
            assignee?.let { it.points = maxOf(0, it.points - 10) }
            task.streak = maxOf(0, task.streak - 1)
        }
        FirebaseSync.pushTask(task)
        assignee?.let { FirebaseSync.pushUser(it) }
    }

    fun addTask(title: String, assignee: String) {
        val task = Task(title = title, assignedTo = assignee)
        tasks.add(task)
        FirebaseSync.pushTask(task)
    }

    fun rotateTasks() {
        val members = getWGMembers().map { it.name }.sorted()
        if (members.isEmpty()) return
        tasks.forEach { task ->
            val idx = members.indexOf(task.assignedTo)
            task.assignedTo = members[(idx + 1) % members.size]
            task.completed = false
        }
        FirebaseSync.pushAllTasks()
    }

    fun removeTask(task: Task) {
        tasks.remove(task)
    }

    fun addTask(task: Task) {
        tasks.add(task)
        FirebaseSync.pushTask(task)
    }

    fun strikeUser(userName: String) {
        users.find { it.name == userName }?.let {
            it.points = maxOf(0, it.points - 15)
            FirebaseSync.pushUser(it)
        }
    }

    // ── Rewards helpers ─────────────────────────────────────
    fun redeemReward(reward: RewardItem): Boolean {
        val user = currentUser ?: return false
        if (user.points < reward.cost) return false
        user.points -= reward.cost
        val ticket = Ticket(type = TicketType.KUDOS, text = "${user.name} redeemed: ${reward.emoji} ${reward.title}", author = "System")
        tickets.add(ticket)
        FirebaseSync.pushUser(user)
        FirebaseSync.pushTicket(ticket)
        return true
    }

    fun addReward(reward: RewardItem) {
        rewards.add(reward)
        FirebaseSync.pushReward(reward)
    }

    fun removeReward(reward: RewardItem) {
        rewards.remove(reward)
    }

    // ── Recipes / Meals ─────────────────────────────────────
    fun getRecipeById(id: String?): Recipe? = recipes.find { it.id == id }

    fun addIngredientsToShopping(recipe: Recipe) {
        recipe.ingredients.forEach { ing ->
            val item = ShoppingItem(name = ing, price = 0.0, addedBy = currentUser?.name ?: "")
            shoppingItems.add(item)
            FirebaseSync.pushShoppingItem(item)
        }
    }

    // ── Firebase-aware CRUD (for screens that mutate lists directly) ──
    fun syncUser(user: User) { FirebaseSync.pushUser(user) }

    fun addEvent(event: CalendarEvent) { events.add(event); FirebaseSync.pushEvent(event) }
    fun removeEvent(event: CalendarEvent) { events.remove(event); FirebaseSync.removeEvent(event.id) }
    fun clearPastEvents(today: String): Boolean {
        val past = events.filter { it.date < today }
        if (past.isEmpty()) return false
        past.forEach { FirebaseSync.removeEvent(it.id) }
        events.removeAll(past.toSet())
        return true
    }

    fun addTicket(ticket: Ticket) { tickets.add(ticket); FirebaseSync.pushTicket(ticket) }
    fun syncTicket(ticket: Ticket) { FirebaseSync.pushTicket(ticket) }

    fun addRecipe(recipe: Recipe) { recipes.add(recipe); FirebaseSync.pushRecipe(recipe) }
    fun removeRecipe(recipe: Recipe) { recipes.remove(recipe); FirebaseSync.removeRecipe(recipe.id) }

    fun addVaultItem(item: VaultItem) { vaultItems.add(item); FirebaseSync.pushVaultItem(item) }
    fun removeVaultItem(item: VaultItem) { vaultItems.remove(item); FirebaseSync.removeVaultItem(item.id) }
    fun updateVaultItem(item: VaultItem) { FirebaseSync.pushVaultItem(item) }

    fun addUser(user: User) { users.add(user); FirebaseSync.pushUser(user) }

    // ── Join Request helpers ────────────────────────────────
    fun sendJoinRequest(wgId: String, message: String = ""): Boolean {
        val user = currentUser ?: return false
        // Don't allow duplicate requests
        if (pendingRequests.any { it.userId == user.id && it.wgId == wgId }) return false
        val request = JoinRequest(
            userName = user.name,
            userEmail = user.email,
            userId = user.id,
            message = message,
            wgId = wgId
        )
        pendingRequests.add(request)
        FirebaseSync.pushJoinRequest(request)
        return true
    }

    fun acceptJoinRequest(request: JoinRequest): Boolean {
        val wg = wgs.find { it.id == request.wgId } ?: return false
        val requestUser = users.find { it.id == request.userId }
        if (requestUser != null) {
            requestUser.wgId = wg.id
            requestUser.hasWG = true
            FirebaseSync.pushUser(requestUser)
        } else {
            // Create the user from the request data
            val newUser = User(
                name = request.userName,
                email = request.userEmail,
                wgId = wg.id,
                hasWG = true,
                avatarEmoji = "👤"
            )
            users.add(newUser)
            FirebaseSync.pushUser(newUser)
        }
        pendingRequests.remove(request)
        FirebaseSync.removeJoinRequest(request.id)
        return true
    }

    fun rejectJoinRequest(request: JoinRequest) {
        pendingRequests.remove(request)
        FirebaseSync.removeJoinRequest(request.id)
    }

    fun getPendingRequestsForMyWG(): List<JoinRequest> {
        val wgId = currentUser?.wgId ?: return emptyList()
        return pendingRequests.filter { it.wgId == wgId }
    }

    fun updateWGShowcase(rentPrice: Int, description: String) {
        val wg = currentWG ?: return
        wg.rentPrice = rentPrice
        wg.publicDescription = description
        FirebaseSync.pushWG(wg)
    }

    fun addAmenity(amenity: String) {
        val wg = currentWG ?: return
        if (!wg.amenities.contains(amenity)) {
            wg.amenities.add(amenity)
            FirebaseSync.pushWG(wg)
        }
    }

    fun removeAmenity(amenity: String) {
        val wg = currentWG ?: return
        wg.amenities.remove(amenity)
        FirebaseSync.pushWG(wg)
    }

    fun getAvailableAmenities(): List<Pair<String, String>> = listOf(
        "wifi" to "📶", "washer" to "🧺", "dryer" to "👕", "parking" to "🅿️",
        "balcony" to "🌿", "garden" to "🌳", "dishwasher" to "🍽️", "elevator" to "🛗",
        "bike_storage" to "🚲", "cellar" to "📦", "bathtub" to "🛁", "tv" to "📺"
    )

    fun updateWGRules(rules: String) {
        val wg = currentWG ?: return
        wg.wgRules = rules
        FirebaseSync.pushWG(wg)
    }

    fun updateWGBudget(budget: Double) {
        val wg = currentWG ?: return
        wg.monthlyBudget = budget
        FirebaseSync.pushWG(wg)
    }

    fun getWGRules(): String = currentWG?.wgRules ?: ""
    fun getMonthlyBudget(): Double = currentWG?.monthlyBudget ?: 400.0

    fun getUserAvatarEmoji(userName: String): String {
        return users.find { it.name == userName }?.avatarEmoji ?: "👤"
    }

    fun saveMealPlanDay(day: MealPlanDay) { FirebaseSync.pushMealPlanDay(day) }

    fun resetAllTasks() {
        tasks.forEach { it.completed = false }
        FirebaseSync.pushAllTasks()
    }

    fun clearBoughtItems() {
        FirebaseSync.clearShoppingBought()
        shoppingItems.removeAll { it.status == ShoppingStatus.BOUGHT }
    }

    fun resetAllPasswords() {
        users.forEach { it.password = "1234" }
        FirebaseSync.pushAllUsers()
    }

    // ── Super Admin helpers ──────────────────────────────────
    fun banUser(user: User) {
        user.isBanned = true
        FirebaseSync.pushUser(user)
        addSystemLog("🚫 Banned user: ${user.name}")
    }

    fun unbanUser(user: User) {
        user.isBanned = false
        FirebaseSync.pushUser(user)
        addSystemLog("✅ Unbanned user: ${user.name}")
    }

    fun promoteToAdmin(user: User) {
        user.role = UserRole.ADMIN
        FirebaseSync.pushUser(user)
        addSystemLog("👑 Promoted ${user.name} to ADMIN")
    }

    fun demoteToUser(user: User) {
        user.role = UserRole.USER
        FirebaseSync.pushUser(user)
        addSystemLog("⬇️ Demoted ${user.name} to USER")
    }

    fun impersonateUser(user: User) {
        originalSuperAdmin = currentUser
        currentUser = user
        currentWG = wgs.find { it.id == user.wgId }
        addSystemLog("👁️ Impersonating: ${user.name}")
    }

    fun stopImpersonation() {
        val admin = originalSuperAdmin ?: return
        currentUser = admin
        currentWG = wgs.find { it.id == admin.wgId }
        originalSuperAdmin = null
        addSystemLog("↩️ Stopped impersonation")
    }

    fun isImpersonating(): Boolean = originalSuperAdmin != null

    fun toggleMaintenanceMode() {
        maintenanceMode = !maintenanceMode
        addSystemLog(if (maintenanceMode) "🔒 Maintenance Mode ENABLED" else "🔓 Maintenance Mode DISABLED")
    }

    fun sendBroadcast(message: String) {
        broadcastMessage = message
        addSystemLog("📢 Broadcast: $message")
    }

    fun clearBroadcast() {
        broadcastMessage = ""
    }

    fun nukeDatabase() {
        shoppingItems.clear()
        tasks.clear()
        tickets.clear()
        events.clear()
        recipes.clear()
        mealPlan.clear()
        vaultItems.clear()
        rewards.clear()
        pendingRequests.clear()
        recurringCosts.clear()
        guestPasses.clear()
        smartScenes.clear()
        pantryItems.clear()
        // Keep users and WGs intact
        FirebaseSync.pushAll()
        addSystemLog("🔥 DATABASE NUKED — all content data cleared")
    }

    fun addSystemLog(message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.GERMANY)
        systemLogs.add(0, "[${sdf.format(Date())}] $message")
        if (systemLogs.size > 50) systemLogs.removeAt(systemLogs.lastIndex)
    }

    fun getSystemUptime(): String = "99.9%"

    // ── Super Admin WG management ────────────────────────────
    fun getWGMembers(wgId: String): List<User> = users.filter { it.wgId == wgId && it.hasWG }

    fun getWGAdmin(wgId: String): User? = users.find { it.wgId == wgId && it.role == UserRole.ADMIN }

    fun deleteWG(wg: WG) {
        // Remove all members from WG
        users.filter { it.wgId == wg.id }.forEach { u ->
            u.wgId = ""
            u.hasWG = false
            FirebaseSync.pushUser(u)
        }
        wgs.remove(wg)
        FirebaseSync.removeWG(wg.id)
        addSystemLog("🗑️ Deleted WG: ${wg.name}")
    }

    fun removeUserFromWG(user: User) {
        user.wgId = ""
        user.hasWG = false
        user.onboardingCompleted = false
        if (user.role == UserRole.ADMIN) user.role = UserRole.USER
        FirebaseSync.pushUser(user)
        addSystemLog("👤 Removed ${user.name} from WG")
    }

    fun addUserToWG(user: User, wgId: String) {
        user.wgId = wgId
        user.hasWG = true
        FirebaseSync.pushUser(user)
        addSystemLog("➕ Added ${user.name} to WG")
    }

    fun setWGAdmin(user: User, wgId: String) {
        // Demote current admin
        users.filter { it.wgId == wgId && it.role == UserRole.ADMIN }.forEach { old ->
            old.role = UserRole.USER
            FirebaseSync.pushUser(old)
        }
        // Promote new admin
        user.role = UserRole.ADMIN
        user.wgId = wgId
        user.hasWG = true
        FirebaseSync.pushUser(user)
        addSystemLog("👑 Set ${user.name} as admin of WG")
    }

    fun getUsersWithoutWG(): List<User> = users.filter { !it.hasWG && it.role != UserRole.SUPER_ADMIN }

    // ── Recurring Costs helpers ─────────────────────────────
    fun addRecurringCost(name: String, emoji: String, amount: Double, paidBy: String) {
        val cost = RecurringCost(name = name, emoji = emoji, totalAmount = amount, paidBy = paidBy)
        recurringCosts.add(cost)
        FirebaseSync.pushRecurringCost(cost)
    }

    fun removeRecurringCost(cost: RecurringCost) {
        recurringCosts.remove(cost)
        FirebaseSync.removeRecurringCost(cost.id)
    }

    fun toggleRecurringCost(cost: RecurringCost) {
        cost.isActive = !cost.isActive
        FirebaseSync.pushRecurringCost(cost)
    }

    fun updateRecurringCost(cost: RecurringCost) {
        FirebaseSync.pushRecurringCost(cost)
    }

    fun getRecurringCostPerPerson(): Double {
        val members = getWGMembers()
        if (members.isEmpty()) return 0.0
        return recurringCosts.filter { it.isActive }.sumOf { it.totalAmount } / members.size
    }

    fun getRecurringCostTotal(): Double {
        return recurringCosts.filter { it.isActive }.sumOf { it.totalAmount }
    }

    // ── Wall of Shame/Glory helpers ─────────────────────────
    fun getLeaderboard(): List<Pair<User, Int>> {
        return getWGMembers().map { user ->
            val completedTasks = tasks.count { it.completed && it.assignedTo == user.name }
            val shoppingDone = shoppingItems.count { it.status == ShoppingStatus.BOUGHT && it.boughtBy == user.name }
            val score = user.points + (completedTasks * 5) + (shoppingDone * 3)
            user to score
        }.sortedByDescending { it.second }
    }

    fun getBadgeForRank(rank: Int, total: Int): String {
        return when {
            rank == 0 -> "👑" // crown for #1
            rank == total - 1 && total > 1 -> "🤡" // clown for last
            rank == total - 2 && total > 2 -> "🐌" // snail for second-to-last
            else -> "⭐"
        }
    }

    fun sendKudos(toUserName: String) {
        val from = currentUser?.name ?: return
        val ticket = Ticket(type = TicketType.KUDOS, text = "$from sent kudos to $toUserName! 🌟", author = "System")
        tickets.add(ticket)
        users.find { it.name == toUserName }?.let {
            it.points += 5
            FirebaseSync.pushUser(it)
        }
        FirebaseSync.pushTicket(ticket)
    }

    fun sendShame(toUserName: String) {
        val from = currentUser?.name ?: return
        val ticket = Ticket(type = TicketType.COMPLAINT, text = "$from shamed $toUserName! 😬", author = "System")
        tickets.add(ticket)
        users.find { it.name == toUserName }?.let {
            it.points = maxOf(0, it.points - 5)
            FirebaseSync.pushUser(it)
        }
        FirebaseSync.pushTicket(ticket)
    }

    // ── Guest Pass helpers ──────────────────────────────────
    fun createGuestPass(guestName: String, wifiPassword: String = ""): GuestPass {
        val wifi = wifiPassword.ifEmpty {
            vaultItems.find { it.type == VaultType.WIFI }?.value ?: ""
        }
        val pass = GuestPass(
            guestName = guestName,
            createdBy = currentUser?.name ?: "",
            wgId = currentUser?.wgId ?: "",
            wifiPassword = wifi
        )
        guestPasses.add(pass)
        FirebaseSync.pushGuestPass(pass)
        return pass
    }

    fun revokeGuestPass(pass: GuestPass) {
        pass.isActive = false
        FirebaseSync.pushGuestPass(pass)
    }

    fun removeGuestPass(pass: GuestPass) {
        guestPasses.remove(pass)
        FirebaseSync.removeGuestPass(pass.id)
    }

    fun getActiveGuestPasses(): List<GuestPass> {
        val wgId = currentUser?.wgId ?: return emptyList()
        return guestPasses.filter { it.wgId == wgId && it.isActive }
    }

    // ── Smart Home helpers ──────────────────────────────────
    fun toggleSmartScene(scene: SmartScene) {
        scene.isActive = !scene.isActive
        if (scene.isActive) {
            // Send notification as a ticket
            val ticket = Ticket(
                type = TicketType.KUDOS,
                text = scene.notificationText,
                author = currentUser?.name ?: "Smart Home"
            )
            tickets.add(ticket)
            FirebaseSync.pushTicket(ticket)
        }
        FirebaseSync.pushSmartScene(scene)
    }

    fun addSmartScene(name: String, emoji: String, description: String, notificationText: String) {
        val scene = SmartScene(name = name, emoji = emoji, description = description, notificationText = notificationText)
        smartScenes.add(scene)
        FirebaseSync.pushSmartScene(scene)
    }

    fun removeSmartScene(scene: SmartScene) {
        smartScenes.remove(scene)
        FirebaseSync.removeSmartScene(scene.id)
    }

    // ── Onboarding helpers ──────────────────────────────────
    fun initOnboarding(user: User) {
        user.onboardingSteps = mutableListOf(
            OnboardingItem(OnboardingStepType.READ_RULES),
            OnboardingItem(OnboardingStepType.ADD_IBAN),
            OnboardingItem(OnboardingStepType.PICK_CLEANING_DAY),
            OnboardingItem(OnboardingStepType.SET_AVATAR),
            OnboardingItem(OnboardingStepType.INTRODUCE_YOURSELF),
        )
        user.onboardingCompleted = false
        FirebaseSync.pushUser(user)
    }

    fun completeOnboardingStep(stepType: OnboardingStepType) {
        val user = currentUser ?: return
        user.onboardingSteps.find { it.type == stepType }?.completed = true
        if (user.onboardingSteps.all { it.completed }) {
            user.onboardingCompleted = true
            user.points += 50 // bonus for completing onboarding
        }
        FirebaseSync.pushUser(user)
    }

    fun getOnboardingProgress(): Float {
        val steps = currentUser?.onboardingSteps ?: return 1f
        if (steps.isEmpty()) return 1f
        return steps.count { it.completed }.toFloat() / steps.size
    }

    // ── Analytics helpers ───────────────────────────────────
    fun getCompletedTasksCount() = tasks.count { it.completed }
    fun getTotalSpent() = shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }.sumOf { it.price }
    fun getTopContributor(): String {
        val members = getWGMembers()
        return members.maxByOrNull { u -> tasks.count { it.completed && it.assignedTo == u.name } }?.name ?: "-"
    }
    fun getTasksByPerson(): Map<String, Int> {
        return getWGMembers().associate { u -> u.name to tasks.count { it.completed && it.assignedTo == u.name } }
    }

    // ── Pantry helpers ──────────────────────────────────────
    fun addPantryItem(name: String, emoji: String): PantryItem {
        val item = PantryItem(name = name, emoji = emoji, updatedBy = currentUser?.name ?: "")
        pantryItems.add(item)
        FirebaseSync.pushPantryItem(item)
        return item
    }

    fun updatePantryStatus(item: PantryItem, status: PantryStatus) {
        item.status = status
        item.updatedBy = currentUser?.name ?: ""
        FirebaseSync.pushPantryItem(item)
    }

    fun removePantryItem(item: PantryItem) {
        pantryItems.remove(item)
        FirebaseSync.removePantryItem(item.id)
    }
}

private fun generateCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..5).map { chars.random() }.joinToString("")
}
