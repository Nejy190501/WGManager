package com.example.wgmanager.data

import com.google.firebase.database.FirebaseDatabase

/**
 * Handles all Firebase Realtime Database read/write operations.
 * DataStore remains the in-memory source of truth; FirebaseSync persists changes.
 */
object FirebaseSync {

    private val db by lazy { FirebaseDatabase.getInstance().reference }

    // ═══════════════════════════════════════════════════════════════
    // LOAD ALL DATA FROM FIREBASE
    // ═══════════════════════════════════════════════════════════════
    fun loadAll(onComplete: (Boolean) -> Unit) {
        db.get().addOnSuccessListener { snap ->
            if (!snap.exists() || !snap.hasChild("users")) {
                onComplete(false)
                return@addOnSuccessListener
            }
            try {
                DataStore.users.clear()
                snap.child("users").children.forEach { c -> parseUser(c)?.let { DataStore.users.add(it) } }

                DataStore.wgs.clear()
                snap.child("wgs").children.forEach { c -> parseWG(c)?.let { DataStore.wgs.add(it) } }

                DataStore.shoppingItems.clear()
                snap.child("shoppingItems").children.forEach { c -> parseShoppingItem(c)?.let { DataStore.shoppingItems.add(it) } }

                DataStore.tasks.clear()
                snap.child("tasks").children.forEach { c -> parseTask(c)?.let { DataStore.tasks.add(it) } }

                DataStore.tickets.clear()
                snap.child("tickets").children.forEach { c -> parseTicket(c)?.let { DataStore.tickets.add(it) } }

                DataStore.events.clear()
                snap.child("events").children.forEach { c -> parseEvent(c)?.let { DataStore.events.add(it) } }

                DataStore.recipes.clear()
                snap.child("recipes").children.forEach { c -> parseRecipe(c)?.let { DataStore.recipes.add(it) } }

                DataStore.mealPlan.clear()
                snap.child("mealPlan").children.forEach { c -> parseMealPlanDay(c)?.let { DataStore.mealPlan.add(it) } }

                DataStore.vaultItems.clear()
                snap.child("vaultItems").children.forEach { c -> parseVaultItem(c)?.let { DataStore.vaultItems.add(it) } }

                DataStore.rewards.clear()
                snap.child("rewards").children.forEach { c -> parseReward(c)?.let { DataStore.rewards.add(it) } }

                DataStore.pendingRequests.clear()
                snap.child("pendingRequests").children.forEach { c -> parseJoinRequest(c)?.let { DataStore.pendingRequests.add(it) } }

                DataStore.recurringCosts.clear()
                snap.child("recurringCosts").children.forEach { c -> parseRecurringCost(c)?.let { DataStore.recurringCosts.add(it) } }

                DataStore.guestPasses.clear()
                snap.child("guestPasses").children.forEach { c -> parseGuestPass(c)?.let { DataStore.guestPasses.add(it) } }

                DataStore.smartScenes.clear()
                snap.child("smartScenes").children.forEach { c -> parseSmartScene(c)?.let { DataStore.smartScenes.add(it) } }

                DataStore.pantryItems.clear()
                snap.child("pantryItems").children.forEach { c -> parsePantryItem(c)?.let { DataStore.pantryItems.add(it) } }

                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }.addOnFailureListener {
            it.printStackTrace()
            onComplete(false)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PUSH ALL DATA TO FIREBASE (initial seed)
    // ═══════════════════════════════════════════════════════════════
    fun pushAll() {
        val root = hashMapOf<String, Any>(
            "users" to DataStore.users.associate { it.id to it.toMap() },
            "wgs" to DataStore.wgs.associate { it.id to it.toMap() },
            "shoppingItems" to DataStore.shoppingItems.associate { it.id to it.toMap() },
            "tasks" to DataStore.tasks.associate { it.id to it.toMap() },
            "tickets" to DataStore.tickets.associate { it.id to it.toMap() },
            "events" to DataStore.events.associate { it.id to it.toMap() },
            "recipes" to DataStore.recipes.associate { it.id to it.toMap() },
            "mealPlan" to DataStore.mealPlan.associate { it.day.lowercase() to it.toMap() },
            "vaultItems" to DataStore.vaultItems.associate { it.id to it.toMap() },
            "rewards" to DataStore.rewards.associate { it.id to it.toMap() },
            "pendingRequests" to DataStore.pendingRequests.associate { it.id to it.toMap() },
            "recurringCosts" to DataStore.recurringCosts.associate { it.id to it.toMap() },
            "guestPasses" to DataStore.guestPasses.associate { it.id to it.toMap() },
            "smartScenes" to DataStore.smartScenes.associate { it.id to it.toMap() },
            "pantryItems" to DataStore.pantryItems.associate { it.id to it.toMap() },
        )
        db.setValue(root)
    }

    // ═══════════════════════════════════════════════════════════════
    // INDIVIDUAL PUSH / REMOVE OPERATIONS
    // ═══════════════════════════════════════════════════════════════
    fun pushUser(user: User) = db.child("users").child(user.id).setValue(user.toMap())
    fun pushWG(wg: WG) = db.child("wgs").child(wg.id).setValue(wg.toMap())
    fun removeWG(id: String) = db.child("wgs").child(id).removeValue()
    fun pushShoppingItem(item: ShoppingItem) = db.child("shoppingItems").child(item.id).setValue(item.toMap())
    fun removeShoppingItem(id: String) = db.child("shoppingItems").child(id).removeValue()
    fun pushTask(task: Task) = db.child("tasks").child(task.id).setValue(task.toMap())
    fun removeTask(id: String) = db.child("tasks").child(id).removeValue()
    fun pushAllTasks() = db.child("tasks").setValue(DataStore.tasks.associate { it.id to it.toMap() })
    fun pushTicket(ticket: Ticket) = db.child("tickets").child(ticket.id).setValue(ticket.toMap())
    fun pushEvent(event: CalendarEvent) = db.child("events").child(event.id).setValue(event.toMap())
    fun removeEvent(id: String) = db.child("events").child(id).removeValue()
    fun pushRecipe(recipe: Recipe) = db.child("recipes").child(recipe.id).setValue(recipe.toMap())
    fun removeRecipe(id: String) = db.child("recipes").child(id).removeValue()
    fun pushMealPlanDay(day: MealPlanDay) = db.child("mealPlan").child(day.day.lowercase()).setValue(day.toMap())
    fun pushVaultItem(item: VaultItem) = db.child("vaultItems").child(item.id).setValue(item.toMap())
    fun removeVaultItem(id: String) = db.child("vaultItems").child(id).removeValue()
    fun pushReward(reward: RewardItem) = db.child("rewards").child(reward.id).setValue(reward.toMap())
    fun removeReward(id: String) = db.child("rewards").child(id).removeValue()
    fun pushJoinRequest(request: JoinRequest) = db.child("pendingRequests").child(request.id).setValue(request.toMap())
    fun removeJoinRequest(id: String) = db.child("pendingRequests").child(id).removeValue()
    fun pushAllUsers() = db.child("users").setValue(DataStore.users.associate { it.id to it.toMap() })
    fun pushRecurringCost(cost: RecurringCost) = db.child("recurringCosts").child(cost.id).setValue(cost.toMap())
    fun removeRecurringCost(id: String) = db.child("recurringCosts").child(id).removeValue()
    fun pushGuestPass(pass: GuestPass) = db.child("guestPasses").child(pass.id).setValue(pass.toMap())
    fun removeGuestPass(id: String) = db.child("guestPasses").child(id).removeValue()
    fun pushSmartScene(scene: SmartScene) = db.child("smartScenes").child(scene.id).setValue(scene.toMap())
    fun removeSmartScene(id: String) = db.child("smartScenes").child(id).removeValue()
    fun pushPantryItem(item: PantryItem) = db.child("pantryItems").child(item.id).setValue(item.toMap())
    fun removePantryItem(id: String) = db.child("pantryItems").child(id).removeValue()
    fun clearShoppingBought() {
        DataStore.shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }.forEach {
            db.child("shoppingItems").child(it.id).removeValue()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DATA CLASS → MAP (serialization)
    // ═══════════════════════════════════════════════════════════════
    private fun User.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "wgId" to wgId, "name" to name, "email" to email,
        "password" to password, "role" to role.name, "hasWG" to hasWG,
        "points" to points, "status" to status.name,
        "isTwoFactorEnabled" to isTwoFactorEnabled, "bio" to bio,
        "avatarEmoji" to avatarEmoji, "themeColor" to themeColor.name,
        "isDarkMode" to isDarkMode, "language" to language,
        "notificationsEnabled" to notificationsEnabled,
        "onboardingCompleted" to onboardingCompleted,
        "monthlyBadge" to monthlyBadge,
        "isBanned" to isBanned,
        "onboardingSteps" to onboardingSteps.map { mapOf("type" to it.type.name, "completed" to it.completed) }
    )

    private fun WG.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "address" to address, "joinCode" to joinCode,
        "rentPrice" to rentPrice, "publicDescription" to publicDescription,
        "wgRules" to wgRules, "monthlyBudget" to monthlyBudget,
        "amenities" to amenities
    )

    private fun ShoppingItem.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "emoji" to emoji, "price" to price, "addedBy" to addedBy,
        "boughtBy" to boughtBy, "status" to status.name, "date" to date
    )

    private fun Task.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "title" to title, "assignedTo" to assignedTo,
        "completed" to completed, "streak" to streak
    )

    private fun Ticket.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "type" to type.name, "text" to text, "author" to author,
        "isSolved" to isSolved, "pollOptions" to pollOptions,
        "pollVotes" to pollVotes, "date" to date
    )

    private fun CalendarEvent.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "title" to title, "date" to date, "type" to type.name,
        "emoji" to emoji, "createdBy" to createdBy, "isRecurring" to isRecurring
    )

    private fun Recipe.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "emoji" to emoji, "difficulty" to difficulty,
        "timeMinutes" to timeMinutes, "ingredients" to ingredients
    )

    private fun MealPlanDay.toMap(): Map<String, Any?> = mapOf(
        "day" to day, "recipeId" to recipeId, "cook" to cook
    )

    private fun VaultItem.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "label" to label, "value" to value, "type" to type.name,
        "customIcon" to customIcon, "isSecure" to isSecure
    )

    private fun RewardItem.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "emoji" to emoji, "title" to title, "cost" to cost,
        "description" to description
    )

    private fun JoinRequest.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "userName" to userName, "userEmail" to userEmail,
        "userId" to userId, "message" to message, "wgId" to wgId, "date" to date
    )

    private fun RecurringCost.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "emoji" to emoji, "totalAmount" to totalAmount,
        "frequency" to frequency.name, "paidBy" to paidBy, "isActive" to isActive
    )

    private fun GuestPass.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "guestName" to guestName, "createdBy" to createdBy,
        "wgId" to wgId, "accessCode" to accessCode, "wifiPassword" to wifiPassword,
        "isActive" to isActive, "createdDate" to createdDate
    )

    private fun SmartScene.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "emoji" to emoji, "description" to description,
        "notificationText" to notificationText, "isActive" to isActive
    )

    private fun PantryItem.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "emoji" to emoji, "status" to status.name,
        "updatedBy" to updatedBy, "date" to date
    )

    // ═══════════════════════════════════════════════════════════════
    // MAP → DATA CLASS (deserialization)
    // ═══════════════════════════════════════════════════════════════
    private fun parseUser(snap: com.google.firebase.database.DataSnapshot): User? {
        val m = snap.value as? Map<*, *> ?: return null
        return User(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            email = m["email"] as? String ?: "",
            password = m["password"] as? String ?: "1234",
            wgId = m["wgId"] as? String ?: "",
            role = enumSafe(m["role"] as? String, UserRole.USER),
            hasWG = m["hasWG"] as? Boolean ?: false,
            points = (m["points"] as? Number)?.toInt() ?: 0,
            status = enumSafe(m["status"] as? String, UserStatus.ONLINE),
            isTwoFactorEnabled = m["isTwoFactorEnabled"] as? Boolean ?: false,
            bio = m["bio"] as? String ?: "",
            avatarEmoji = m["avatarEmoji"] as? String ?: "👤",
            themeColor = enumSafe(m["themeColor"] as? String, ThemeColor.INDIGO),
            isDarkMode = m["isDarkMode"] as? Boolean ?: false,
            language = m["language"] as? String ?: "EN",
            notificationsEnabled = m["notificationsEnabled"] as? Boolean ?: true,
            onboardingCompleted = m["onboardingCompleted"] as? Boolean ?: false,
            monthlyBadge = m["monthlyBadge"] as? String ?: "",
            isBanned = m["isBanned"] as? Boolean ?: false,
            onboardingSteps = parseOnboardingSteps(m["onboardingSteps"])
        )
    }

    private fun parseWG(snap: com.google.firebase.database.DataSnapshot): WG? {
        val m = snap.value as? Map<*, *> ?: return null
        return WG(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            address = m["address"] as? String ?: "",
            joinCode = m["joinCode"] as? String ?: "",
            rentPrice = (m["rentPrice"] as? Number)?.toInt() ?: 0,
            publicDescription = m["publicDescription"] as? String ?: "",
            wgRules = m["wgRules"] as? String ?: "",
            monthlyBudget = (m["monthlyBudget"] as? Number)?.toDouble() ?: 400.0,
            amenities = (m["amenities"] as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf("wifi", "washer")
        )
    }

    private fun parseShoppingItem(snap: com.google.firebase.database.DataSnapshot): ShoppingItem? {
        val m = snap.value as? Map<*, *> ?: return null
        return ShoppingItem(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            emoji = m["emoji"] as? String ?: "📦",
            price = (m["price"] as? Number)?.toDouble() ?: 0.0,
            addedBy = m["addedBy"] as? String ?: "",
            boughtBy = m["boughtBy"] as? String ?: "",
            status = enumSafe(m["status"] as? String, ShoppingStatus.PENDING),
            date = m["date"] as? String ?: ""
        )
    }

    private fun parseTask(snap: com.google.firebase.database.DataSnapshot): Task? {
        val m = snap.value as? Map<*, *> ?: return null
        return Task(
            id = m["id"] as? String ?: snap.key ?: return null,
            title = m["title"] as? String ?: "",
            assignedTo = m["assignedTo"] as? String ?: "",
            completed = m["completed"] as? Boolean ?: false,
            streak = (m["streak"] as? Number)?.toInt() ?: 0
        )
    }

    private fun parseTicket(snap: com.google.firebase.database.DataSnapshot): Ticket? {
        val m = snap.value as? Map<*, *> ?: return null
        @Suppress("UNCHECKED_CAST")
        val votes = (m["pollVotes"] as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()
        @Suppress("UNCHECKED_CAST")
        val options = (m["pollOptions"] as? List<String>) ?: emptyList()
        return Ticket(
            id = m["id"] as? String ?: snap.key ?: return null,
            type = enumSafe(m["type"] as? String, TicketType.COMPLAINT),
            text = m["text"] as? String ?: "",
            author = m["author"] as? String ?: "",
            isSolved = m["isSolved"] as? Boolean ?: false,
            pollOptions = options,
            pollVotes = votes,
            date = m["date"] as? String ?: ""
        )
    }

    private fun parseEvent(snap: com.google.firebase.database.DataSnapshot): CalendarEvent? {
        val m = snap.value as? Map<*, *> ?: return null
        return CalendarEvent(
            id = m["id"] as? String ?: snap.key ?: return null,
            title = m["title"] as? String ?: "",
            date = m["date"] as? String ?: "",
            type = enumSafe(m["type"] as? String, EventType.GENERAL),
            emoji = m["emoji"] as? String ?: "📅",
            createdBy = m["createdBy"] as? String ?: "",
            isRecurring = m["isRecurring"] as? Boolean ?: false
        )
    }

    private fun parseRecipe(snap: com.google.firebase.database.DataSnapshot): Recipe? {
        val m = snap.value as? Map<*, *> ?: return null
        @Suppress("UNCHECKED_CAST")
        val ings = (m["ingredients"] as? List<String>) ?: emptyList()
        return Recipe(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            emoji = m["emoji"] as? String ?: "🍽️",
            difficulty = m["difficulty"] as? String ?: "Easy",
            timeMinutes = (m["timeMinutes"] as? Number)?.toInt() ?: 30,
            ingredients = ings
        )
    }

    private fun parseMealPlanDay(snap: com.google.firebase.database.DataSnapshot): MealPlanDay? {
        val m = snap.value as? Map<*, *> ?: return null
        return MealPlanDay(
            day = m["day"] as? String ?: snap.key?.replaceFirstChar { it.uppercase() } ?: return null,
            recipeId = m["recipeId"] as? String,
            cook = m["cook"] as? String ?: ""
        )
    }

    private fun parseVaultItem(snap: com.google.firebase.database.DataSnapshot): VaultItem? {
        val m = snap.value as? Map<*, *> ?: return null
        return VaultItem(
            id = m["id"] as? String ?: snap.key ?: return null,
            label = m["label"] as? String ?: "",
            value = m["value"] as? String ?: "",
            type = enumSafe(m["type"] as? String, VaultType.TEXT),
            customIcon = m["customIcon"] as? String ?: "🔑",
            isSecure = m["isSecure"] as? Boolean ?: false
        )
    }

    private fun parseReward(snap: com.google.firebase.database.DataSnapshot): RewardItem? {
        val m = snap.value as? Map<*, *> ?: return null
        return RewardItem(
            id = m["id"] as? String ?: snap.key ?: return null,
            emoji = m["emoji"] as? String ?: "",
            title = m["title"] as? String ?: "",
            cost = (m["cost"] as? Number)?.toInt() ?: 0,
            description = m["description"] as? String ?: ""
        )
    }

    private fun parseJoinRequest(snap: com.google.firebase.database.DataSnapshot): JoinRequest? {
        val m = snap.value as? Map<*, *> ?: return null
        return JoinRequest(
            id = m["id"] as? String ?: snap.key ?: return null,
            userName = m["userName"] as? String ?: "",
            userEmail = m["userEmail"] as? String ?: "",
            userId = m["userId"] as? String ?: "",
            message = m["message"] as? String ?: "",
            wgId = m["wgId"] as? String ?: "",
            date = m["date"] as? String ?: ""
        )
    }

    private fun parseRecurringCost(snap: com.google.firebase.database.DataSnapshot): RecurringCost? {
        val m = snap.value as? Map<*, *> ?: return null
        return RecurringCost(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            emoji = m["emoji"] as? String ?: "💸",
            totalAmount = (m["totalAmount"] as? Number)?.toDouble() ?: 0.0,
            frequency = enumSafe(m["frequency"] as? String, RecurringFrequency.MONTHLY),
            paidBy = m["paidBy"] as? String ?: "",
            isActive = m["isActive"] as? Boolean ?: true
        )
    }

    private fun parseGuestPass(snap: com.google.firebase.database.DataSnapshot): GuestPass? {
        val m = snap.value as? Map<*, *> ?: return null
        return GuestPass(
            id = m["id"] as? String ?: snap.key ?: return null,
            guestName = m["guestName"] as? String ?: "",
            createdBy = m["createdBy"] as? String ?: "",
            wgId = m["wgId"] as? String ?: "",
            accessCode = m["accessCode"] as? String ?: "",
            wifiPassword = m["wifiPassword"] as? String ?: "",
            isActive = m["isActive"] as? Boolean ?: true,
            createdDate = m["createdDate"] as? String ?: ""
        )
    }

    private fun parseSmartScene(snap: com.google.firebase.database.DataSnapshot): SmartScene? {
        val m = snap.value as? Map<*, *> ?: return null
        return SmartScene(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            emoji = m["emoji"] as? String ?: "",
            description = m["description"] as? String ?: "",
            notificationText = m["notificationText"] as? String ?: "",
            isActive = m["isActive"] as? Boolean ?: false
        )
    }

    private fun parsePantryItem(snap: com.google.firebase.database.DataSnapshot): PantryItem? {
        val m = snap.value as? Map<*, *> ?: return null
        return PantryItem(
            id = m["id"] as? String ?: snap.key ?: return null,
            name = m["name"] as? String ?: "",
            emoji = m["emoji"] as? String ?: "📦",
            status = enumSafe(m["status"] as? String, PantryStatus.FULL),
            updatedBy = m["updatedBy"] as? String ?: "",
            date = m["date"] as? String ?: ""
        )
    }

    // ── Enum helper ─────────────────────────────────────────
    private inline fun <reified T : Enum<T>> enumSafe(value: String?, default: T): T {
        return try { if (value != null) enumValueOf<T>(value) else default } catch (_: Exception) { default }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOnboardingSteps(raw: Any?): MutableList<OnboardingItem> {
        val list = raw as? List<*> ?: return mutableListOf()
        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val type = try { enumValueOf<OnboardingStepType>(m["type"] as? String ?: "") } catch (_: Exception) { return@mapNotNull null }
            OnboardingItem(type = type, completed = m["completed"] as? Boolean ?: false)
        }.toMutableList()
    }
}
