package com.example.qrscanner.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrscanner.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountsViewModel(private val database: AppDatabase, context: Context) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> get() = _isInitialized

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "AppPreferences", Context.MODE_PRIVATE)

    private val _accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val accounts: StateFlow<List<AccountEntity>> get() = _accounts

    private val _currentAccount = MutableStateFlow<AccountEntity?>(null)
    val currentAccount: StateFlow<AccountEntity?> get() = _currentAccount

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val lastSelectedAccountId = preferences.getInt("lastSelectedAccountId", -1)
            _currentAccount.value = database.appDao().getAccountById(id = lastSelectedAccountId)
            val allAccounts = database.appDao().getAllAccounts()

            if (_currentAccount.value == null && allAccounts.isNotEmpty()){
                Log.e("Accounts","Аккаунтов больше одного. Не удалось найти сохранённый" +
                        "выбранный аккаунт. Выбран первый аккаунт из списка")

                saveCurrentAccount(allAccounts.first())
            }

            val currentAccountId: Int = _currentAccount.value?.id ?: -1

            Log.d("Accounts","All accounts size: ${allAccounts.size}," +
                            " last selected account id: $currentAccountId")

            // Сортируем: выбранный аккаунт — первый, остальные — по алфавиту
            val sortedAccounts = allAccounts.sortedWith(compareByDescending<AccountEntity> {
                it.id == currentAccountId // Сначала выбранный аккаунт
            }.thenBy { it.login }) // Затем по алфавиту
            _accounts.value = sortedAccounts

            _isInitialized.value = true
        }
    }

    fun addAccount(login: String, password: String) {
        viewModelScope.launch {
            val newAccount = AccountEntity(login = login, password = password)
            val allAccounts = database.appDao().getAllAccounts()
            // Если это первый аккаунт, сделать его текущим
            if (allAccounts.size == 1) {
                saveCurrentAccount(newAccount)
            }
            database.appDao().insertAccount(newAccount)
            loadAccounts()
        }
    }

    fun selectAccount(account: AccountEntity) {
        saveCurrentAccount(account)
        loadAccounts() // Перезагружаем список аккаунтов после обновления
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            database.appDao().updateAccount(account)
            loadAccounts() // Перезагружаем список аккаунтов после обновления
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            database.appDao().deleteAccount(account.id)
            // Если удаляемый аккаунт был выбран
            if (_currentAccount.value?.id == account.id) {
                val remainingAccounts = database.appDao().getAllAccounts()
                // Выбираем первый аккаунт, если есть
                val remainingAccount = remainingAccounts.firstOrNull()
                saveCurrentAccount(remainingAccount)
            }

            loadAccounts() // Обновляем список аккаунтов после удаления
        }
    }

    private fun saveCurrentAccount(account: AccountEntity?) {
        val accountId = account?.id ?: -1
        preferences.edit().putInt("lastSelectedAccountId", accountId).apply()
        _currentAccount.value = account
    }
}
