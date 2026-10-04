package com.pennywiseai.tracker.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pennywiseai.tracker.data.database.migration.MIGRATION_63_64
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PennyWiseDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate63To64() {
        // Create the database in version 63
        var db = helper.createDatabase(TEST_DB, 63)
        // You could insert some data here if needed
        db.close()

        // Run the migration to version 64
        db = helper.runMigrationsAndValidate(TEST_DB, 64, true, MIGRATION_63_64)
        
        // Ensure indices were created
        val cursor = db.query("PRAGMA index_list('transactions')")
        val indices = mutableListOf<String>()
        while (cursor.moveToNext()) {
            indices.add(cursor.getString(1))
        }
        cursor.close()
        
        assert(indices.contains("index_transactions_is_deleted_date_time"))
        assert(indices.contains("index_transactions_category"))
        assert(indices.contains("index_transactions_transaction_type"))
    }
}
