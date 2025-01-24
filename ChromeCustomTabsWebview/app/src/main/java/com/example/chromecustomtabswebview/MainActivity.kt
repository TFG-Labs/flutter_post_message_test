package com.example.chromecustomtabswebview

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.browser.trusted.TrustedWebActivityIntentBuilder
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import com.example.chromecustomtabswebview.databinding.ActivityMainBinding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var mClient: CustomTabsClient? = null
    private var mSession: CustomTabsSession? = null
    private val URL: Uri = Uri.parse("https://peconn.github.io/starters")
//    private val URL: Uri = Uri.parse("https://staging.tfglabs.dev/giftero")

//    private val SOURCE_ORIGIN: Uri = Uri.parse("https://staging.tfglabs.dev")
//    private val TARGET_ORIGIN: Uri = Uri.parse("https://staging.tfglabs.dev")
    private var SOURCE_ORIGIN: Uri = Uri.parse("https://sayedelabady.github.io/")
    private val TARGET_ORIGIN: Uri = Uri.parse("https://peconn.github.io")
    private var mValidated = false

    private val TAG = "PostMessageDemo"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        MessageNotificationHandler.createNotificationChannelIfNeeded(this);

        bindCustomTabsService(this)
    }

    private val customTabsCallback: CustomTabsCallback = object : CustomTabsCallback() {
        override fun onPostMessage(message: String, @Nullable extras: Bundle?) {
            super.onPostMessage(message, extras)
            Log.d(TAG, "DOES ONPOSTMESSAGE EVER GET CALLED!!")
            if (message.contains("ACK")) {
                Log.d(TAG, "RETURNING FOR ACK")
                return
            }
            MessageNotificationHandler.showNotificationWithMessage(this@MainActivity, "BLAH BLAH BLAAAAAAH FUUUCK")
            Log.d(TAG, "Got message: $message")
        }

        override fun onRelationshipValidationResult(
            relation: Int, requestedOrigin: Uri,
            result: Boolean, @Nullable extras: Bundle?
        ) {
            // If this fails:
            // - Have you called warmup?
            // - Have you set up Digital Asset Links correctly?
            // - Double check what browser you're using.
            Log.d(TAG, "Relationship result: $result")
            mValidated = result
        }

        // Listens for navigation, requests the postMessage channel when one completes.
        override fun onNavigationEvent(navigationEvent: Int, @Nullable extras: Bundle?) {
            if (navigationEvent != NAVIGATION_FINISHED) {
                return
            }

            if (!mValidated) {
                Log.d(TAG, "Not starting PostMessage as validation didn't succeed.")
            }

            // If this fails:
            // - Have you included PostMessageService in your AndroidManifest.xml?
            val result = mSession?.requestPostMessageChannel(
                SOURCE_ORIGIN, TARGET_ORIGIN,
                Bundle()
            )
            Log.d(TAG, "Requested Post Message Channel: $result")
        }

        override fun onMessageChannelReady(@Nullable extras: Bundle?) {
            Log.d(TAG, "Message channel ready.")

            Executors.newSingleThreadScheduledExecutor().schedule({
                val result = mSession?.postMessage("First message", null)
                Log.d(TAG, "postMessage returned: $result")
            }, 20, TimeUnit.SECONDS)

        }
    }

    private fun bindCustomTabsService(context: Context) {
        val packageName = CustomTabsClient.getPackageName(context, null)
        Toast.makeText(this, "Binding to $packageName", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Binding to $packageName")
        CustomTabsClient.bindCustomTabsService(
            this, packageName,
            object : CustomTabsServiceConnection() {
                override fun onCustomTabsServiceConnected(
                    name: ComponentName,
                    client: CustomTabsClient
                ) {
                    Log.d(TAG, "onCustomTabsServiceConnected")
                    mClient = client

                    // Note: validateRelationship requires warmup to have been called.
                    client.warmup(0L)

                    mSession = mClient?.newSession(customTabsCallback)
                    mSession?.validateRelationship(CustomTabsService.RELATION_USE_AS_ORIGIN, SOURCE_ORIGIN, Bundle())

                    launch()
                    registerBroadcastReceiver()
                }

                override fun onServiceDisconnected(componentName: ComponentName) {
                    mClient = null
                }
            })
    }

    private fun launch() {
//        if(mSession != null) {
            Log.d(TAG, "LAUNCH CALLED")
            TrustedWebActivityIntentBuilder(URL).build(mSession!!)
                .launchTrustedWebActivity(this@MainActivity)
//        }
    }

    @SuppressLint("WrongConstant")
    private fun registerBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(PostMessageBroadcastReceiver.POST_MESSAGE_ACTION)
        ContextCompat.registerReceiver(
            this, PostMessageBroadcastReceiver(mSession),
            intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}