package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tinygc.okodukai.data.local.entity.SavingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingGoalEntity)

    @Delete
    suspend fun delete(goal: SavingGoalEntity)

    @Query("SELECT * FROM saving_goals WHERE id = :id")
    suspend fun getById(id: String): SavingGoalEntity?

    @Query("SELECT * FROM saving_goals ORDER BY display_order ASC, created_at ASC")
    suspend fun getAll(): List<SavingGoalEntity>

    @Query("SELECT * FROM saving_goals ORDER BY display_order ASC, created_at ASC")
    fun getAllFlow(): Flow<List<SavingGoalEntity>>
}
