package tech.harrynull.mywatcard

import android.content.Context
import android.graphics.PorterDuff
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.balance_header.view.*
import kotlinx.android.synthetic.main.transaction_item.view.*
import org.w3c.dom.Text
import kotlin.math.floor
import kotlin.math.round

class WatCardListAdapter(private val context: Context, private val watcard: WatCard)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val BALANCE_HEADER = 1
//        private val FOOTER_VIEW = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == BALANCE_HEADER)
            return BalanceHolder(
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
        if (position == 0) return BALANCE_HEADER
//        if (position == itemCount - 1) return FOOTER_VIEW
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionHolder -> holder.bind(watcard.transactions!![position - 1])
            is BalanceHolder -> holder.bind(watcard.getFlexibleAccount(), watcard.getMealPlanAccount())
            else -> {
                //
            }
        }
    }

    override fun getItemCount(): Int = (watcard.transactions?.size ?: 0) + 1

    inner class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionAmountView: TextView = itemView.transaction_item_amount
        private val transactionTimeView: TextView = itemView.transaction_item_time
        private val transactionDescriptionView: TextView = itemView.transaction_item_description
        private val transactionIcon: ImageView = itemView.transaction_item_icon

        fun bind(transaction: Transaction) {
            transactionAmountView.text = transaction.amount
            transactionTimeView.text = transaction.getDisplayDate()
            transactionDescriptionView.text = transaction.getShortTerminalName()
            val icon = transaction.getIcon()
            transactionIcon.setImageDrawable(context.getDrawable(icon.drawableId))
            transactionIcon.background.setColorFilter(ContextCompat.getColor(context, icon.colorId), PorterDuff.Mode.MULTIPLY)
        }
    }

    inner class BalanceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val balanceDollarView: TextView = itemView.balance_dollar_view
        private val balanceCentView: TextView = itemView.balance_cent_view
        private val balanceCurrencyView: TextView = itemView.balance_currency_view
        private val balanceAccountView: TextView = itemView.balance_account_view
        private val balanceFlexDollarAmountView: TextView = itemView.balance_1
        private val balanceMealPlanAmountView: TextView = itemView.balance_2

        fun bind(flex: Account, meal: Account) {
            val amountFlex = flex.getAmountAsDouble()
            val amountMeal = meal.getAmountAsDouble()
            val amount = amountFlex + amountMeal
            val amountInt = floor(amount).toInt()
            balanceDollarView.text = amountInt.toString()
            balanceCentView.text = round((amount - amountInt) * 100).toInt().toString()
            balanceCurrencyView.text = "$"
            // balanceAccountView.text = amountFlex.toString()
            balanceFlexDollarAmountView.text = amountFlex.toString()
            balanceMealPlanAmountView.text = amountMeal.toString()
        }
    }
}