package tech.harrynull.mywatcard

import android.app.AlertDialog
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_dialog.view.*

class MainActivity : AppCompatActivity() {

    fun getPreferences(database: String = "") : SharedPreferences =
        if(database != "") getSharedPreferences(database, MODE_PRIVATE)
        else PreferenceManager.getDefaultSharedPreferences(applicationContext)

    fun setPreference(key: String, value: Any, database: String = "") {
        if (key == "") {
            throw NullPointerException(String.format("Key and value not be null key=%s, value=%s", key, value))
        }
        val edit = getPreferences(database).edit()

        when (value) {
            is String -> edit.putString(key, value)
            is Int -> edit.putInt(key, value)
            is Long -> edit.putLong(key, value)
            is Boolean -> edit.putBoolean(key, value)
            is Float -> edit.putFloat(key, value)
            else -> throw IllegalArgumentException(String.format("Type of value unsupported key=%s, value=%s", key, value))
        }
        edit.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (getPreferences("account").getString("username", "") == "") {
            // construct view
            val dialog = this.layoutInflater.inflate(R.layout.login_dialog, null)
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setView(dialog)
            dialogBuilder.setTitle("Login")
            dialogBuilder.setPositiveButton("OK") { _, _ ->
                setPreference("username", dialog.username.text.toString(), "account")
                setPreference("password", dialog.password.text.toString(), "account")
            }
            dialogBuilder.create().show()
            return
        }
        val accountDatabase = getPreferences("account")
        val watcard = WatCard(accountDatabase.getString("username", "")!!, accountDatabase.getString("password", "")!!)
        Thread {
            watcard.login()
            watcard.fetch()
            var logStr = ""
            logStr += "LOG: \n"
            for (account in watcard.accounts!!){
                if(account.amount != "$0.00")
                    logStr += "${account.name}: ${account.getAmountAsDouble()}\n"
            }
            runOnUiThread {
                //log.text = logStr
                transaction_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                transaction_recycler.adapter = TransactionListAdapter(watcard.transactions)
                transaction_recycler.invalidate()
            }
        }.start()
    }
}
