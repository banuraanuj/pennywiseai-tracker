package com.pennywiseai.tracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pennywiseai.tracker.data.database.entity.CustomTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTemplateDao {

    @Query("SELECT * FROM custom_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<CustomTemplateEntity>>

    @Query("SELECT * FROM custom_templates ORDER BY createdAt DESC")
    suspend fun getAllTemplatesSync(): List<CustomTemplateEntity>

    @Query("SELECT * FROM custom_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): CustomTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CustomTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: CustomTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: CustomTemplateEntity)

    @Query("DELETE FROM custom_templates")
    suspend fun deleteAllTemplates()
}