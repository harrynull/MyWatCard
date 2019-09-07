package tech.harrynull.mywatcard

val ICON_MAP = mapOf("LAUNDRY" to "")

class Transaction(watCard: WatCard, val date: String, val amount: String, val accountId: String,
                  val units: String, val type: String, val terminal: String) {
    val account: Account? = watCard.findAccount(accountId)

    fun getAmountAsDouble() = amount.replace("$","").replace(",","").toDouble()
    fun getShortTerminalName() = terminal.substringAfter(": ")
    fun getIcon() = ICON_MAP.filter { terminal.contains(it.key) }.entries.firstOrNull()?.value ?: "Default"
}