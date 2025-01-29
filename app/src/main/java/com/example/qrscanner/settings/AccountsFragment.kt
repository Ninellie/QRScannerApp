package com.example.qrscanner.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.R
import com.example.qrscanner.database.AppDatabase
import kotlinx.coroutines.launch

class AccountsFragment : Fragment(R.layout.fragment_accounts) {

    private val viewModel: AccountsViewModel by viewModels {
        AccountsViewModelFactory(AppDatabase.getInstance(requireContext()), requireContext())
    }

    private lateinit var adapter: AccountsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Accounts", "onCreate")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Accounts", "onViewCreated")
        lifecycleScope.launch {
            viewModel.isInitialized.collect { isInitialized ->
                if (isInitialized) {
                    Log.d("Accounts", "accounts fragment init start")
                    createAdapter()
                    setupRecyclerView(view)
                    setupAddAccountButton(view)
                    setupNoAccountsTextPlug(view)
                }
            }
        }
    }

    private fun createAdapter(){
        Log.i("Accounts", "creating Adapter")

        this.adapter = AccountsAdapter(
            currentAccountId = viewModel.currentAccount.value?.id,
            onAccountSelected = { account ->
                viewModel.selectAccount(account)
            },
            onAccountEdit = { account ->
                showEditAccountDialog(account)
            },
            onAccountDelete = { account ->
                showAccountDeletionDialog(account)
            }
        )

        // Подписываемся на изменения currentAccount, чтобы обновлять адаптер
        lifecycleScope.launch {
            viewModel.currentAccount.collect { currentAccount ->
                adapter.updateCurrentAccount(currentAccount?.id)
            }
        }
    }

    private fun setupAddAccountButton(view: View){
        val addAccountButton = view.findViewById<Button>(R.id.addAccountButton)

        addAccountButton?.setOnClickListener {
            showAddAccountDialog()
        }
    }

    private fun setupNoAccountsTextPlug(view: View){
        val noAccountsText = view.findViewById<TextView>(R.id.noAccountsText)

        // Обновление списка аккаунтов из ViewModel
        lifecycleScope.launch {
            viewModel.accounts.collect { accounts ->
                adapter.submitList(accounts)
                noAccountsText?.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupRecyclerView(view: View){
        Log.i("Accounts", "RecyclerView setup")
        val recyclerView = view.findViewById<RecyclerView>(R.id.accountsRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showEditAccountDialog(account: AccountEntity) {
        val dialog = AccountEditDialogFragment(account) { updatedAccount ->
            viewModel.updateAccount(updatedAccount)
        }
        dialog.show(parentFragmentManager, "EditAccountDialog")
    }

    private fun showAccountDeletionDialog(account: AccountEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Вы точно хотите удалить аккаунт: ${account.login}?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.deleteAccount(account)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun showAddAccountDialog() {
        val dialog = AccountAddDialogFragment { login, password ->
            viewModel.addAccount(login, password)
        }
        dialog.show(parentFragmentManager, "AddAccountDialog")
    }
}
