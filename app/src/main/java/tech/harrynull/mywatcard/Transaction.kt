package tech.harrynull.mywatcard

import java.text.SimpleDateFormat
import java.util.*

const val MILLIS_PER_DAY = 86400000L
val ICON_MAP = mapOf("FS-" to IconInfo(R.drawable.ic_restaurant_menu_black_24dp, R.color.icon_background))
val DEFAULT_ICON = IconInfo(R.drawable.ic_dehaze_black_24dp, R.color.icon_background)

class Transaction(watCard: WatCard, val date: String, val amount: String, val accountId: String,
                  val units: String, val type: String, val terminal: String) {
    val account: Account? = watCard.findAccount(accountId)

    fun getAmountAsDouble() = amount.replace("$","").replace(",","").toDouble()
    fun getShortTerminalName() = terminal.substringAfter(": ")
    fun getIcon() = ICON_MAP.filter { terminal.contains(it.key) }.entries.firstOrNull()?.value ?: DEFAULT_ICON
    fun getDisplayDate() : String {
        val date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.CANADA).parse(this.date)
        val currentDate = Date()
        val dateInDays = date.time / MILLIS_PER_DAY
        val currentDateInDays = currentDate.time / MILLIS_PER_DAY

        return when {
            dateInDays == currentDateInDays -> {
                val sdf = SimpleDateFormat("HH:mm", Locale.CANADA)
                sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
                sdf.format(date.time)
            }
            currentDateInDays - dateInDays <= 6 -> {
                val sdf = SimpleDateFormat("EEEE", Locale.CANADA)
                sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
                sdf.format(date.time)
            }
            else -> {
                val sdf = SimpleDateFormat("MMM d", Locale.CANADA)
                sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
                sdf.format(date.time)
            }
        }
    }

}