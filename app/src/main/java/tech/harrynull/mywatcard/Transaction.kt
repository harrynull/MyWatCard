package tech.harrynull.mywatcard

class Transaction(val date: String, val amount: String, val account: String,
                  val units: String, val type: String, val terminal: String) {
    fun getAmountAsDouble() = amount.replace("$","").replace(",","").toDouble()
    fun getShortTerminalName() = terminal.substringAfter(": ")
}