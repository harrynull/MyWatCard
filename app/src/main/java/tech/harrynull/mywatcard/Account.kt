package tech.harrynull.mywatcard

class Account(val number: String, val name: String, val limit: String, val amount: String){
    fun getLimitAsDouble() = limit.replace("$","").replace(",","").toDouble()
    fun getAmountAsDouble() = amount.replace("$","").replace(",","").toDouble()
}