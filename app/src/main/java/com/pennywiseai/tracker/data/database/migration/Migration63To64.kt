package com.pennywiseai.tracker.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_63_64 = object : Migration(63, 64) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_is_deleted_date_time` ON `transactions` (`is_deleted`, `date_time`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_transaction_type` ON `transactions` (`transaction_type`)")
    }
}
