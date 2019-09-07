package tech.harrynull.mywatcard

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.transaction_item.view.*

class TransactionListAdapter(private var transactions: List<Transaction>?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TransactionHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.transaction_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TransactionHolder).bind(transactions!![position])
    }

    override fun getItemCount(): Int = transactions?.size ?: 0

    inner class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val transactionAccountView: TextView = itemView.transaction_account
        private val transactionAmountView: TextView = itemView.transaction_amount
        private val transactionDateView: TextView = itemView.transaction_date
        private val transactionTerminalView: TextView = itemView.transaction_terminal

        fun bind(transaction: Transaction) {
            transactionAccountView.text = transaction.account
            transactionAmountView.text = transaction.amount
            transactionDateView.text = transaction.date
            transactionTerminalView.text = transaction.terminal
        }
    }
}