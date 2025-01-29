package com.example.qrscanner.settings

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.qrscanner.R

class AccountAddDialogFragment(
    private val onAccountAdded: (String, String) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_account, null)

        val loginEditText = view.findViewById<EditText>(R.id.loginEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Добавить аккаунт")
            .setView(view)
            .setPositiveButton("Добавить") { _, _ ->
                val login = loginEditText.text.toString()
                val password = passwordEditText.text.toString()
                onAccountAdded(login, password)
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = false // Изначально кнопка выключена

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Проверяем логин и пароль
                    val isLoginValid = isUrlValid(loginEditText.text.toString())
                    val isPasswordValid = isBearerTokenValid(passwordEditText.text.toString())

                    addButton.isEnabled = isLoginValid && isPasswordValid

                    // Подсказки пользователю
                    if (!isLoginValid) {
                        loginEditText.error = "Unvalid URL. Url starts with http(s)://"
                    } else {
                        loginEditText.error = null
                    }

                    if (!isPasswordValid) {
                        passwordEditText.error = "Unvalid token"
                    } else {
                        passwordEditText.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }

            loginEditText.addTextChangedListener(textWatcher)
            passwordEditText.addTextChangedListener(textWatcher)
        }

        return dialog
    }

    // todo вернуть систему логина
    private fun isLoginValid(login: String): Boolean {
        val regex = "^[a-zA-Z0-9 ]{3,30}$".toRegex() // Только латинские буквы и цифры
        return login.matches(regex)
    }

    private fun isPasswordValid(password: String): Boolean {
        val regex = "^[a-zA-Z0-9!@#\$%^&*() \\-_=+.]{6,60}$".toRegex() // Латинские буквы, цифры, спецсимволы
        return password.matches(regex)
    }

    private fun isUrlValid(url: String): Boolean {
        return url.startsWith("https://") || url.startsWith("http://")
            //"^(https?://)?[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;%=]+$".toRegex() // Простая проверка URL
    }

    private fun isBearerTokenValid(token: String): Boolean {
        val regex = "^[A-Za-z0-9 |\\-._~+/]+=*$".toRegex() // Base64-like structure with space and |
        return token.matches(regex)
    }
}