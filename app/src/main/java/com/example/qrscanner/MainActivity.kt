package com.example.qrscanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import com.example.qrscanner.history.HistoryFragment
import com.example.qrscanner.scanner.ScannerFragment
import com.example.qrscanner.settings.AccountsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        // Устанавливаем начальный экран (главный экран)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ScannerFragment())
                .commit()
        }

        // Обработка кликов по нижней панели навигации
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_accounts -> {
                    replaceFragment(AccountsFragment())
                    true
                }
                R.id.nav_home -> {
                    replaceFragment(ScannerFragment())
                    true
                }
                R.id.nav_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}