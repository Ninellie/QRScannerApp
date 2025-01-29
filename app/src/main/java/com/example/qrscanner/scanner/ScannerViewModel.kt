package com.example.qrscanner.scanner

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.qrscanner.QrCodeRequest
import com.example.qrscanner.RetrofitInstance
import com.example.qrscanner.database.AppDatabase
import com.example.qrscanner.database.ReceiptEntity
import com.example.qrscanner.settings.AccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

class ScannerViewModel(
    private val database: AppDatabase,
    private val context: Context
) : ViewModel() {
    private val _currentAccount = MutableStateFlow<AccountEntity?>(null)

    val currentAccount: StateFlow<AccountEntity?> get() = _currentAccount

    private val preferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    private var lastScannedCode: String? = null

    private val _scannedResult = MutableLiveData<ScanResult>()
    val scannedResult: LiveData<ScanResult> get() = _scannedResult

    // Список обязательных параметров
    private val requiredParams = listOf("iic", "tin", "crtd", "prc", "sw", "cr")

    init {
        loadCurrentAccount()
    }

    fun handleScannedCode(rawValue: String) {
        if (rawValue == lastScannedCode) return // Пропускаем дублирование
        lastScannedCode = rawValue

        if (_currentAccount.value == null){
            _scannedResult.value = ScanResult.Invalid("Создайте аккаунт")
            return
        }

        if (!isValidQrCode(rawValue)) {
            _scannedResult.value = ScanResult.Invalid("Код не прошёл верификацию")
            return
        }

        viewModelScope.launch {
            try {
                val isAdded = addCodeToDatabase(rawValue)
                if (isAdded) {
                    _scannedResult.value = ScanResult.Valid
                    sendCodeToServer(rawValue)
                } else {
                    _scannedResult.value =
                        ScanResult.Invalid("Код уже существует в базе")
                }
            } catch (e: Exception) {
                _scannedResult.value = ScanResult.Invalid("Ошибка добавления: ${e.message}")
            }
        }
    }


    private fun loadCurrentAccount() {
        val lastSelectedAccountId = preferences.getInt("lastSelectedAccountId", -1)
        if (lastSelectedAccountId != -1) {
            viewModelScope.launch {
                _currentAccount.value = database.appDao().getAccountById(lastSelectedAccountId)
            }
        }
    }

    private fun isValidQrCode(rawValue: String): Boolean {
        val uri = Uri.parse(rawValue)
        Log.i("Scanner", "rawValue: $rawValue")

        // Извлекаем фрагмент (часть после #)
        val fragment = uri.fragment
        Log.i("Scanner", "Fragment: $fragment")

        if (fragment.isNullOrEmpty()) {
            Log.e("Scanner", "Fragment null or empty")
            return false
        }

        try {
            // Преобразуем фрагмент в полноценный иерархический URI
            val fragmentUri = Uri.parse("http://dummy$fragment")
            val params = fragmentUri.queryParameterNames
            Log.i("Scanner", "Параметров: ${params.size}. Параметры: $params")

            // Проверяем наличие обязательных параметров
            return requiredParams.all { it in params }
        } catch (e: UnsupportedOperationException) {
            Log.e("Scanner", "Ошибка при обработке фрагмента: ${e.message}")
            return false
        }
    }

    // Возвращает false если код уже был добавлен или произошла ошибка
    private suspend fun addCodeToDatabase(rawValue: String): Boolean {
        if (database.appDao().isReceiptExists(rawValue)) return false

        val account = getCurrentAccount()
        if (account == null) {
            Log.e("Database", "Не выбран аккаунт. Добавление отменено.")
            return false
        }

        val uri = Uri.parse(rawValue)
        val fragment = uri.fragment ?: return false
        val fragmentUri = Uri.parse("http://dummy$fragment")

        val price = fragmentUri.getQueryParameter("prc")?.toFloatOrNull() ?: 0f
        val purchaseTime = fragmentUri.getQueryParameter("crtd")?.let {
            parseDateTimeToLong(it)
        } ?: -1L

        val iic = fragmentUri.getQueryParameter("iic") ?: ""
        val tin = fragmentUri.getQueryParameter("tin") ?: ""

        val newCode = ReceiptEntity(
            linkString = rawValue,
            scanDateTime = System.currentTimeMillis(),
            status = 0,
            accountId = account.id,
            price = price,
            purchaseTime = purchaseTime,
            iic = iic,
            tin = tin,
            comment = "",
            items = ""
        )

        database.appDao().insertReceipt(newCode)
        return true
    }

    private fun sendCodeToServer(qrCode: String) {
        val account = getCurrentAccount()

        if (account == null) {
            Log.e("Server", "Не выбран аккаунт. Отправка отменена.")
            return
        }

        val token = account.password

        val baseUrl = account.login

        val encodedQrCode = urlEncode(qrCode)

        viewModelScope.launch {
            try {
                // todo добавить токены
                val response = RetrofitInstance.api
                    .sendQrCodeWithCustomUrl(baseUrl, token, encodedQrCode)

                if (response.isSuccessful) {
                    Toast.makeText(context,
                        "Success ${response.code()} ${response.message()}",
                        Toast.LENGTH_SHORT).show()

                    Log.d("Server", "${response.code()} ${response.message()}")

                    Log.d("Server", "id: ${response.body()?.data?.id}")
                    Log.d("Server", "user_id: ${response.body()?.data?.user_id}")
                    Log.d("Server", "url: ${response.body()?.data?.url}")
                    Log.d("Server", "iic: ${response.body()?.data?.iic}")
                    Log.d("Server", "tin: ${response.body()?.data?.tin}")
                    Log.d("Server", "prc: ${response.body()?.data?.prc}")
                    Log.d("Server", "crtd: ${response.body()?.data?.crtd}")
                    Log.d("Server", "status: ${response.body()?.data?.status}")
                    Log.d("Server", "created_at: ${response.body()?.data?.created_at}")
                    Log.d("Server", "updated_at: ${response.body()?.data?.updated_at}")
                } else {
                    Toast.makeText(context, "${response.code()} ${response.message()}",
                        Toast.LENGTH_SHORT).show()
                    Log.e("Server", "Ошибка: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Server", "Сетевая ошибка: ${e.message}")
            }
        }
    }

    // Получение текущего аккаунта
    private fun getCurrentAccount(): AccountEntity? {
        val accountId = preferences.getInt("lastSelectedAccountId", -1)
        return if (accountId != -1) {
            _currentAccount.value
        } else {
            null
        }
    }


    private fun urlEncode(input: String): String {
        return try {
            URLEncoder.encode(input, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun parseDateTimeToLong(dateTime: String): Long {
        return try {

            val decodedString = URLDecoder.decode(dateTime, "UTF-8")

            // Исправляем возможный пробел перед временной зоной
            val fixedDateString = decodedString.replace(" ", "+")

            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",
                Locale.getDefault())
            val date = formatter.parse(fixedDateString)
            date?.time ?: throw IllegalArgumentException("Invalid date")
            //date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("DateTimeParser", "Ошибка преобразования даты: ${e.message}")
            -1L // Возвращаем -1 в случае ошибки
            //System.currentTimeMillis()
        }
    }

    sealed class ScanResult {
        data object Valid : ScanResult()
        data class Invalid(val reason: String) : ScanResult()
    }
}