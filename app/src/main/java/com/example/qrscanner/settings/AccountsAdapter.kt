package com.example.qrscanner.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.R

class AccountsAdapter(
    private var currentAccountId: Int?,
    private val onAccountSelected: (AccountEntity) -> Unit,
    private val onAccountEdit: (AccountEntity) -> Unit,
    private val onAccountDelete: (AccountEntity) -> Unit
) : ListAdapter<AccountEntity, AccountsAdapter.Companion.AccountViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = getItem(position)

        holder.bind(account,
            currentAccountId ?: -1,
            onAccountSelected,
            onAccountEdit,
            onAccountDelete)
    }

    fun updateCurrentAccount(newCurrentAccountId: Int?) {
        this.currentAccountId = newCurrentAccountId
        this.notifyDataSetChanged() // Обновляем весь список
    }

    companion object {
        class DiffCallback : DiffUtil.ItemCallback<AccountEntity>() {
            override fun areItemsTheSame(oldItem: AccountEntity, newItem: AccountEntity):
                    Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AccountEntity, newItem: AccountEntity):
                    Boolean {
                return oldItem == newItem
            }
        }

        class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val radioButton: RadioButton = view.findViewById(R.id.radioButton)
            private val loginTextView: TextView = view.findViewById(R.id.loginTextView)
            private val editButton: Button = view.findViewById(R.id.editButton)
            private val deleteButton: Button = view.findViewById(R.id.deleteButton)

            fun bind(
                account: AccountEntity,
                currentAccountId: Int,
                onAccountSelected: (AccountEntity) -> Unit,
                onAccountEdit: (AccountEntity) -> Unit,
                onAccountDelete: (AccountEntity) -> Unit
            ) {
                loginTextView.text = account.login
                Log.d("Accounts", "$currentAccountId")
                radioButton.isChecked = account.id == currentAccountId

                // Клик по строке обрабатывает выбор аккаунта
                itemView.setOnClickListener {
                    if (account.id != currentAccountId) { // Избегаем лишних вызовов
                        onAccountSelected(account)
                    }
                }

                editButton.setOnClickListener { onAccountEdit(account) }
                deleteButton.setOnClickListener { onAccountDelete(account) }
            }
        }
    }
}

