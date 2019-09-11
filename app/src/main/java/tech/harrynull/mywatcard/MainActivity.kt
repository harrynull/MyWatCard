package tech.harrynull.mywatcard

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_dialog.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import android.view.WindowManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*


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

    fun showSnackBar(view: View, msg: String, colorRed: Boolean) {
        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        if (colorRed) snackBar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.warningColor))
        else snackBar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        snackBar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                transaction_swipe_refresh_layout.isRefreshing = true
                onRefreshListener.onRefresh()
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    lateinit var watcard: WatCard

    private val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        Thread {
            try {
                watcard.fetch()
            } catch (e: Exception) {
                showSnackBar(coordinator_layout, e.message ?: "Unknown error occurred", true)
                e.printStackTrace()
            }
            runOnUiThread {
                transaction_recycler.adapter?.notifyDataSetChanged()
                transaction_swipe_refresh_layout.isRefreshing = false
            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // TODO: Change this
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
        watcard = WatCard(accountDatabase.getString("username", "")!!, accountDatabase.getString("password", "")!!)

        transaction_swipe_refresh_layout.isRefreshing = true
        Thread {
            try{
                watcard.login()
                watcard.fetch()
            } catch(e: Exception) {
                showSnackBar(coordinator_layout, e.message ?: "Unknown error occurred", true)
                e.printStackTrace()
            }
            runOnUiThread {
                val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
                itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
                transaction_recycler.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                transaction_recycler.adapter = WatCardListAdapter(this, watcard)
                transaction_recycler.addItemDecoration(itemDecorator)
                transaction_swipe_refresh_layout.isRefreshing = false
            }
        }.start()

        transaction_swipe_refresh_layout.setOnRefreshListener(onRefreshListener)
    }
}
