package com.nexuzy.chatrivo

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.teamxdevelopers.SuperChat.security.ScamProtectionManager

/**
 * ChatrivoApp - Application class for Chatrivo
 * Package: com.nexuzy.chatrivo
 *
 * Initializes:
 *  - Firebase
 *  - Realm
 *  - Scam Protection community blocklist sync
 *  - Any global singletons
 */
class ChatrivoApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        // Firebase
        FirebaseApp.initializeApp(this)

        // Sync community scam blocklist from Firebase
        ScamProtectionManager.syncCommunityBlocklist()
    }

    companion object {
        lateinit var INSTANCE: ChatrivoApp
            private set
    }
}
