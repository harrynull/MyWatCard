package tech.harrynull.mywatcard

import java.text.SimpleDateFormat
import java.util.*

const val MILLIS_PER_DAY = 86400000L
val ICON_MAP = mapOf("FS-" to IconInfo(R.drawable.ic_restaurant_menu_black_24dp, R.color.food))
val DEFAULT_ICON = IconInfo(R.drawable.ic_dehaze_black_24dp, R.color.icon_background)
val DATE_PARSER = SimpleDateFormat("MM/dd/yyyy h:mm:ss a", Locale.US)
val DATE_FORMATTER_TIME = SimpleDateFormat("HH:mm", Locale.US)
val DATE_FORMATTER_WEEK = SimpleDateFormat("EEEE", Locale.US)
val DATE_FORMATTER_DAY = SimpleDateFormat("MMM d", Locale.US)
val TIMEZONE_OFFSET = {
    val cal = Calendar.getInstance()
    cal.timeZone = TimeZone.getTimeZone("America/Toronto")
    cal.get(Calendar.ZONE_OFFSET)
}()

class Transaction(watCard: WatCard, val date: String, val amount: String, val accountId: String,
                  val units: String, val type: String, val terminal: String) {
    val account: Account? = watCard.findAccount(accountId)

    init {
        DATE_PARSER.timeZone = TimeZone.getTimeZone("America/Toronto")
        DATE_FORMATTER_WEEK.timeZone = TimeZone.getTimeZone("America/Toronto")
        DATE_FORMATTER_TIME.timeZone = TimeZone.getTimeZone("America/Toronto")
        DATE_FORMATTER_DAY.timeZone = TimeZone.getTimeZone("America/Toronto")
    }

    fun getAmountAsDouble() = amount.replace("$","").replace(",","").toDouble()
    fun getShortTerminalName() = terminal.substringAfter(": ")
    fun getIcon() = ICON_MAP.filter { terminal.contains(it.key) }.entries.firstOrNull()?.value ?: DEFAULT_ICON

    // Note: not thread safe because of SimpleDateFormat
    fun getDisplayDate() : String {
        val date = DATE_PARSER.parse(this.date)
        val currentDate = Date()
        val dateInDays = (date.time + TIMEZONE_OFFSET) / MILLIS_PER_DAY
        val currentDateInDays = (currentDate.time + TIMEZONE_OFFSET) / MILLIS_PER_DAY

        return when {
            dateInDays == currentDateInDays -> {
                DATE_FORMATTER_TIME.format(date.time)
            }
            currentDateInDays - dateInDays == 1L -> {
                "Yesterday"
            }
            currentDateInDays - dateInDays <= 6L -> {
                DATE_FORMATTER_WEEK.format(date.time)
            }
            else -> {
                DATE_FORMATTER_DAY.format(date.time)
            }
        }
    }

}