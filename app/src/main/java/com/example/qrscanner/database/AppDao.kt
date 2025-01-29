package com.example.qrscanner.database

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qrscanner.settings.AccountEntity

@Dao
interface AppDao {
    // Аккаунты
    @Insert
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): AccountEntity

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: Int)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    // Чеки
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Query("SELECT * FROM receipts WHERE accountId = :accountId ORDER BY purchaseTime DESC")
    suspend fun getReceiptsByAccount(accountId: Int): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE id = :id LIMIT 1")
    suspend fun getReceiptById(id: Int): ReceiptEntity?


    // Проверяет наличие чека с указанным linkString
    @Query("SELECT EXISTS(SELECT 1 FROM receipts WHERE linkString = :url)")
    suspend fun isReceiptExists(url: String): Boolean

    // Удаляет все чеки для указанного аккаунта
    @Query("DELETE FROM receipts WHERE accountId = :accountId")
    suspend fun deleteReceiptsByAccount(accountId: Int){
        Log.d("AppDao", "Deleting all receipts for accountId: $accountId")
    }

    // Удаляет чек с указанным id
    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: Int)

    // Полностью очищает таблицу чеков
    @Query("DELETE FROM receipts")
    suspend fun clearAllReceipts(){
        Log.d("AppDao", "Clearing all receipts from database")
    }

}

//    @Query("SELECT * FROM receipts WHERE linkString = :url LIMIT 1")
//    suspend fun getReceiptByUrl(url: String): ReceiptEntity?
//    @Query("UPDATE receipts SET status = :status WHERE id = :receiptId")
//    suspend fun updateReceiptStatus(receiptId: Int, status: Int)
//
//
//
//    @Query("SELECT * FROM receipts WHERE iic = :iic LIMIT 1")
//    suspend fun getReceiptByIic(iic: String): ReceiptEntity?
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insertScannedData(data: ReceiptEntity)
