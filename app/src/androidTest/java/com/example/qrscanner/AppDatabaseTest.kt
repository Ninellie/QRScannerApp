package com.example.qrscanner

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.qrscanner.database.AppDao
import com.example.qrscanner.database.AppDatabase
import com.example.qrscanner.database.ReceiptEntity
import com.example.qrscanner.settings.AccountEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.appDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAccount_andRetrieve() = runBlocking {
        // Создаём тестовый аккаунт
        val account = AccountEntity(login = "test_user", password = "12345")
        dao.insertAccount(account)

        // Проверяем список аккаунтов
        val accounts = dao.getAllAccounts()
        assertEquals(1, accounts.size)
        assertEquals("test_user", accounts[0].login)
    }

    @Test
    fun insertReceipt_andRetrieveByAccount() = runBlocking {
        // Создаём аккаунт
        val account = AccountEntity(login = "test_user", password = "12345")
        dao.insertAccount(account)

        val savedAccount = dao.getAllAccounts().first()

        // Создаём чек
        val receipt = ReceiptEntity(
            accountId = savedAccount.id,
            scanDateTime = System.currentTimeMillis(),
            linkString = "test_code",
            status = 0,
            price = 12.34f,
            purchaseTime = System.currentTimeMillis(),
            comment = "Test comment",
            iic = "",
            tin = "",
            items = "[{\"name\": \"item1\", \"price\": 5.67}]",
        )
        dao.insertReceipt(receipt)

        // Проверяем список чеков
        val receipts = dao.getReceiptsByAccount(savedAccount.id)
        assertEquals(1, receipts.size)
        assertEquals("test_code", receipts[0].linkString)
    }

    @Test
    fun updateReceiptStatus() = runBlocking {
        // Создаём аккаунт и чек
        val account = AccountEntity(login = "test_user", password = "12345")
        dao.insertAccount(account)

        val savedAccount = dao.getAllAccounts().first()

        val receipt = ReceiptEntity(
            accountId = savedAccount.id,
            scanDateTime = System.currentTimeMillis(),
            linkString = "test_code",
            status = 0,
            price = 12.34f,
            purchaseTime = System.currentTimeMillis(),
            comment = "Test comment",
            iic = "",
            tin = "",
            items = "[{\"name\": \"item1\", \"price\": 5.67}]"
        )
        dao.insertReceipt(receipt)

        // Проверяем обновлённый статус
        val updatedReceipt = dao.getReceiptsByAccount(savedAccount.id).first()
        assertEquals(20, updatedReceipt.status)
    }
}
