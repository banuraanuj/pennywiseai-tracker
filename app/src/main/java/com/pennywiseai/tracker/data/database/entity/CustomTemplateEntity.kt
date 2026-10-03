package com.pennywiseai.tracker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "custom_templates")
data class CustomTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Optional regex or simple substring to match the SMS sender.
     * e.g. "HDFCBK" or "^.*-HDFCBK-S$". If empty, template applies to any sender.
     */
    val senderPattern: String = "",
    
    /**
     * The user-defined template string with tags.
     * e.g. "Your a/c {ACCOUNT} is debited for Rs {AMOUNT} towards {MERCHANT}. Ref {REFERENCE}."
     */
    val messageTemplate: String = "",
    
    /**
     * Optional transaction type to enforce if the template matches.
     * e.g. EXPENSE, INCOME. If null, the parser can attempt to infer it or default to EXPENSE.
     */
    val transactionType: TransactionType? = null,
    
    /** Timestamp when the template was created */
    val createdAt: Long = System.currentTimeMillis()
)