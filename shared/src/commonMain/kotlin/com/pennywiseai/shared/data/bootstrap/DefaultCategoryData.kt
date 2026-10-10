package com.pennywiseai.shared.data.bootstrap

object DefaultCategoryData {
    data class SubCategorySeed(
        val name: String,
        val icon: String? = null
    )

    data class CategorySeed(
        val name: String,
        val colorHex: String,
        val isIncome: Boolean,
        val icon: String? = null,
        val subCategories: List<SubCategorySeed> = emptyList()
    )

    val ALL: List<CategorySeed> = listOf(
        CategorySeed(
            name = "Food & Dining",
            colorHex = "#FC8019",
            isIncome = false,
            icon = "🍔",
            subCategories = listOf(
                SubCategorySeed("Restaurants & Cafes", "☕"),
                SubCategorySeed("Food Delivery", "🛵"),
                SubCategorySeed("Snacks & Sweets", "🍩")
            )
        ),
        CategorySeed(
            name = "Groceries",
            colorHex = "#5AC85A",
            isIncome = false,
            icon = "🛒",
            subCategories = listOf(
                SubCategorySeed("Supermarket & Vegetables", "🥦"),
                SubCategorySeed("Dairy & Milk", "🥛"),
                SubCategorySeed("Household Supplies", "🧹")
            )
        ),
        CategorySeed(
            name = "Transportation",
            colorHex = "#000000",
            isIncome = false,
            icon = "🚗",
            subCategories = listOf(
                SubCategorySeed("Fuel & Petrol", "⛽"),
                SubCategorySeed("Public Transit & Metro", "🚇"),
                SubCategorySeed("Cab & Auto", "🚕"),
                SubCategorySeed("Maintenance & Tolls", "🔧")
            )
        ),
        CategorySeed(
            name = "Bills & Utilities",
            colorHex = "#4CAF50",
            isIncome = false,
            icon = "⚡",
            subCategories = listOf(
                SubCategorySeed("Electricity & Water", "💡"),
                SubCategorySeed("Cooking Gas", "🔥"),
                SubCategorySeed("Internet & Broadband", "🌐"),
                SubCategorySeed("TV & OTT", "📺"),
                SubCategorySeed("Society Maintenance", "🏡")
            )
        ),
        CategorySeed(
            name = "Family & Children",
            colorHex = "#673AB7",
            isIncome = false,
            icon = "👨‍👩‍👧‍👦",
            subCategories = listOf(
                SubCategorySeed("School Fees & Tuition", "📚"),
                SubCategorySeed("Books & Stationery", "✏️"),
                SubCategorySeed("Kids Clothing & Toys", "🧸"),
                SubCategorySeed("Activities & Daycare", "🎨")
            )
        ),
        CategorySeed(
            name = "Healthcare",
            colorHex = "#10847E",
            isIncome = false,
            icon = "🏥",
            subCategories = listOf(
                SubCategorySeed("Medicines & Pharmacy", "💊"),
                SubCategorySeed("Doctor & Clinic", "🩺"),
                SubCategorySeed("Tests & Labs", "🧪"),
                SubCategorySeed("Health Insurance", "🛡️")
            )
        ),
        CategorySeed(
            name = "Housing & Maintenance",
            colorHex = "#FF5722",
            isIncome = false,
            icon = "🏠",
            subCategories = listOf(
                SubCategorySeed("Rent & Housing EMI", "🔑"),
                SubCategorySeed("Home Repair & Hardware", "🔨")
            )
        ),
        CategorySeed(
            name = "Shopping & Lifestyle",
            colorHex = "#FF9900",
            isIncome = false,
            icon = "🛍️",
            subCategories = listOf(
                SubCategorySeed("Clothing & Footwear", "👕"),
                SubCategorySeed("Electronics & Gadgets", "💻"),
                SubCategorySeed("Personal Care & Grooming", "💇")
            )
        ),
        CategorySeed(
            name = "Entertainment",
            colorHex = "#E50914",
            isIncome = false,
            icon = "🍿",
            subCategories = listOf(
                SubCategorySeed("Movies & Outings", "🎟️"),
                SubCategorySeed("Family Trips & Travel", "✈️")
            )
        ),
        CategorySeed(
            name = "Banking",
            colorHex = "#004C8F",
            isIncome = false,
            icon = "💳",
            subCategories = listOf(
                SubCategorySeed("Bank Fees & Charges", "🏦"),
                SubCategorySeed("Loan Repayments", "💸")
            )
        ),
        CategorySeed(
            name = "Investments",
            colorHex = "#00D09C",
            isIncome = false,
            icon = "📈",
            subCategories = listOf(
                SubCategorySeed("Mutual Funds (SIP)", "📊"),
                SubCategorySeed("Stocks & Equity", "📈"),
                SubCategorySeed("Fixed Deposits (FD/RD)", "🏦"),
                SubCategorySeed("Provident Fund (EPF/PPF)", "🛡️"),
                SubCategorySeed("NPS & Retirement", "👴"),
                SubCategorySeed("Gold & SGB", "🥇")
            )
        ),
        CategorySeed(
            name = "Insurance",
            colorHex = "#0066CC",
            isIncome = false,
            icon = "🛡️",
            subCategories = listOf(
                SubCategorySeed("Life & Term Insurance", "📝"),
                SubCategorySeed("Vehicle Insurance", "🚗")
            )
        ),
        CategorySeed(
            name = "Mobile",
            colorHex = "#2A3890",
            isIncome = false,
            icon = "📱",
            subCategories = listOf(
                SubCategorySeed("Mobile Recharge & Postpaid", "📶")
            )
        ),
        CategorySeed(
            name = "Fitness",
            colorHex = "#FF3278",
            isIncome = false,
            icon = "🏋️",
            subCategories = listOf(
                SubCategorySeed("Gym & Sports", "⚽")
            )
        ),
        CategorySeed(
            name = "Salary",
            colorHex = "#4CAF50",
            isIncome = true,
            icon = "💼",
            subCategories = listOf(
                SubCategorySeed("Primary Salary", "💰")
            )
        ),
        CategorySeed(
            name = "Income",
            colorHex = "#4CAF50",
            isIncome = true,
            icon = "💵",
            subCategories = listOf(
                SubCategorySeed("Business & Freelance", "📈"),
                SubCategorySeed("Dividends & Interest", "🪙")
            )
        ),
        CategorySeed(
            name = "Others",
            colorHex = "#757575",
            isIncome = false,
            icon = "📦",
            subCategories = listOf(
                SubCategorySeed("Gifts & Donations", "🎁"),
                SubCategorySeed("Emergency Expenses", "🆘")
            )
        )
    )
}
