package com.example.chromecustomtabswebview

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.browser.customtabs.CustomTabsSession;

class PostMessageBroadcastReceiver(session: CustomTabsSession?) : BroadcastReceiver() {

    private var customTabsSession: CustomTabsSession? = null
    companion object {
        val POST_MESSAGE_ACTION: String = "com.example.postmessage.POST_MESSAGE_ACTION"
    }

    init {
        customTabsSession = session
    }

//    fun PostMessageBroadcastReceiver {
//        customTabsSession = session
//    }

    override fun onReceive(context: Context?, intent: Intent?) {
        customTabsSession?.postMessage("Got it!", null)
    }
}