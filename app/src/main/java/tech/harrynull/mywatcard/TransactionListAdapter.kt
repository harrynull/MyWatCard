package tech.harrynull.mywatcard

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.balance_header.view.*
import kotlinx.android.synthetic.main.transaction_item.view.*

class TransactionListAdapter(private var transactions: List<Transaction>?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val HEADER_VIEW = 1
//        private val FOOTER_VIEW = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER_VIEW)
            return BalanceHeader(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.balance_header,
                    parent,
                    false))
        return TransactionHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.transaction_item,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return HEADER_VIEW
//        if (position == itemCount - 1) return FOOTER_VIEW
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionHolder) {
            holder.bind(transactions!![position - 1])
        } else if (holder is BalanceHeader) {
            holder.bind()
        } else {
            //
        }
    }

    override fun getItemCount(): Int = (transactions?.size ?: -1) + 1

    inner class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val transactionAccountView: TextView = itemView.transaction_account
        private val transactionAmountView: TextView = itemView.transaction_item_amount
        private val transactionTimeView: TextView = itemView.transaction_item_time
        private val transactionDescriptionView: TextView = itemView.transaction_item_description

        fun bind(transaction: Transaction) {
//            transactionAccountView.text = transaction.account
            transactionAmountView.text = transaction.amount
            transactionTimeView.text = transaction.date
            transactionDescriptionView.text = transaction.terminal
        }
    }

    inner class BalanceHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val balanceDollarView: TextView = itemView.balance_dollar_view
        private val balanceCentView: TextView = itemView.balance_cent_view
        private val balanceCurrencyView: TextView = itemView.balance_currency_view
        private val balanceAccountView: TextView = itemView.balance_account_view

        fun bind() {
            balanceDollarView.text = "1,093"
            balanceCentView.text = "99"
            balanceCurrencyView.text = "$"
            balanceAccountView.text = "FLEX"
        }
    }
}