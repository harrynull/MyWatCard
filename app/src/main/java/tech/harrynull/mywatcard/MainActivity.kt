package tech.harrynull.mywatcard

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_dialog.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar


class MainActivity : AppCompatActivity() {

    fun getPreferences(database: String = ""): SharedPreferences =
        if (database != "") getSharedPreferences(database, MODE_PRIVATE)
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
            else -> throw IllegalArgumentException(
                String.format(
                    "Type of value unsupported key=%s, value=%s",
                    key,
                    value
                )
            )
        }
        edit.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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
            for (account in watcard.accounts!!) {
                if (account.amount != "$0.00")
                    logStr += "${account.name}: ${account.getAmountAsDouble()}\n"
            }
            runOnUiThread {
                val itemDecorator = DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
                itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
                transaction_recycler.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                transaction_recycler.adapter = TransactionListAdapter(watcard.transactions)
                transaction_recycler.addItemDecoration(itemDecorator)
            }
        }.start()
    }
}
