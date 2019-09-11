package tech.harrynull.mywatcard

import android.util.Log
import okhttp3.*
import java.net.CookieManager
import java.net.CookiePolicy
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

const val LOGON = "https://watcard.uwaterloo.ca/OneWeb/Account/LogOn"
const val BALANCE = "https://watcard.uwaterloo.ca/OneWeb/Financial/Balances"
const val TRANSACTION = "https://watcard.uwaterloo.ca/OneWeb/Financial/TransactionsPass?dateFrom=%s&dateTo=%s&returnRows=1000"

class WatCard(val account: String, val PIN: String) {
    private lateinit var client : OkHttpClient
    private var loggedIn = false
    var personalInfo: PersonalInfo? = null
    var accounts: List<Account>? = null
    var transactions: List<Transaction>? = null

    fun findAccount(number: String) : Account? = accounts?.find { it.number==number }

    fun getFlexibleAccount() : Account {
        val flexAccounts = accounts?.filter { it.name.contains("FLEX") }!!
        return flexAccounts.find { it.getAmountAsDouble() != 0.0 } ?: flexAccounts.first()
    }

    fun getMealPlanAccount() : Account {
        val mealAccounts = accounts?.filter { it.name == "RESIDENCE PLAN" || it.name.contains("MEAL") || it.name.contains("MP") }!!
        return mealAccounts.find { it.getAmountAsDouble() != 0.0 } ?: mealAccounts.first()
    }

    fun login() {
        // Build the HTTP client
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(UAInjector())
            .build()

        // GET to obtain the RequestVerificationToken.
        val response = client.newCall(Request.Builder().url(LOGON).build()).execute().body
        val responseStr = response?.byteString()?.utf8()
            ?: throw Exception("Login Error: Failed to read the response")
        val token = "__RequestVerificationToken\".*?value=\"(.*?)\"".toRegex().find(responseStr)?.groups?.get(1)?.value
            ?: throw Exception("Login Error: Failed to log in")
        response.close()

        // POST to login
        val responseLogin = client.newCall(Request.Builder().url(LOGON).post(
            FormBody.Builder()
                .add("__RequestVerificationToken", token)
                .add("AccountMode", "0")
                .add("Account", account)
                .add("Password", PIN)
                .build())
            .addHeader("Origin", "https://watcard.uwaterloo.ca")
            .addHeader("Referer", "https://watcard.uwaterloo.ca/OneWeb/Account/LogOn")
            .build()).execute().body

        val responseLoginStr = responseLogin?.byteString()?.utf8()
            ?: throw Exception("Login Error: Failed to read the response")

        responseLogin.close()

        if(responseLoginStr.contains("Invalid Account Or PIN!"))
            throw Exception("Login Error: Invalid Account Or PIN")
        else if(!responseLoginStr.contains("ow-id-fullname-xs"))
            throw Exception("Login Error: Unknown login error")

        // Logged in successfully
        loggedIn = true
        personalInfo = PersonalInfo(
            "fullname-xs\">(.*?)\\.<".toRegex().find(responseLoginStr)?.groups?.get(1)?.value
                ?:throw Exception("Failed to parse name")
        )
    }

    fun fetch() {
        if (!loggedIn) throw Exception("Not logged in. Login before fetching data.")
        fetchBalance()

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)

        fetchTransaction(cal.time, Calendar.getInstance().time)
    }

    fun fetchBalance(){
        // Fetch Balance information.
        val response = client.newCall(Request.Builder().url(BALANCE).build()).execute().body
        val responseStr = response?.byteString()?.utf8()?.replace("\r","")?.replace("\n", "")
            ?: throw Exception("Fetch Error: Failed to read the response")
        val accountsData = "<tr>.*?\"Number:\">(.*?)<.*?\"Name:\">(.*?)<.*?\"Limit:\".*?>(.*?)<.*?\"Amount:\".*?>(.*?)<.*?<\\/tr>"
            .toRegex().findAll(responseStr)
        if (accountsData.none()) throw Exception("Fetch Error: Failed to parse balance data")

        accounts = accountsData.map {
            val values = it.groupValues
            Account(values[1], values[2], values[3], values[4])
        }.toList()

        response.close()

    }

    fun fetchTransaction(from: Date, to: Date){
        val sdf = SimpleDateFormat("yyyy/MM/dd+HH:mm:ss", Locale.CANADA)
        sdf.timeZone = TimeZone.getTimeZone("America/Toronto")

        // Fetch Transaction information.
        val url = String.format(TRANSACTION, sdf.format(from), sdf.format(to))
        val response = client.newCall(Request.Builder().url(url).build())
            .execute().body
        val responseStr = response?.byteString()?.utf8()?.replace("\r","")?.replace("\n", "")
            ?: throw Exception("Fetch Error: Failed to read the response")
        val transactionsData = "Date:\">(.*?)</td>.*?>(.*?)</td>.*?>(.*?)</td>.*?>(.*?)</td>.*?>(.*?)</td>.*?>(.*?)<"
            .toRegex().findAll(responseStr)
        if (transactionsData.none()) throw Exception("Fetch Error: Failed to parse transaction data")

        transactions = transactionsData.map {
            val values = it.groupValues
            Transaction(this, values[1], values[2], values[3], values[4], values[5], values[6])
        }.toList()

        response.close()
    }

}