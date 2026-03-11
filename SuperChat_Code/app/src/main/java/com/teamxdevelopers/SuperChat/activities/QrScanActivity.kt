/*
 * *
 *  * Created by TeamXDevelopers
 *  * Copyright (c) 2023 . All rights reserved.
 *
 */

package com.teamxdevelopers.SuperChat.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.teamxdevelopers.SuperChat.R
import com.teamxdevelopers.SuperChat.activities.main.messaging.ChatActivity
import com.teamxdevelopers.SuperChat.databinding.ActivityQrScanBinding
import com.teamxdevelopers.SuperChat.model.realms.User
import com.teamxdevelopers.SuperChat.utils.ContactUtils
import com.teamxdevelopers.SuperChat.utils.FireConstants
import com.teamxdevelopers.SuperChat.utils.IntentUtils
import com.teamxdevelopers.SuperChat.utils.MyApp
import com.teamxdevelopers.SuperChat.utils.NetworkHelper
import com.teamxdevelopers.SuperChat.utils.RealmHelper
import com.teamxdevelopers.SuperChat.utils.SnackbarUtil

class QrScanActivity : BaseActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ActivityQrScanBinding
    var dialogBuilder: AlertDialog.Builder? = null
    var b: AlertDialog? = null
    override fun enablePresence(): Boolean {
        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar!!.title="QR code scan"

        window.statusBarColor=getColor(R.color.colorPrimary)

        codeScanner = CodeScanner(this, binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                goForword(it.text)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {

            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    private fun goForword(value: String?) {
        val uri:Uri = Uri.parse(value)
        val data = uri
        val scheme = data!!.scheme
        val host = data.host
        val params = data.pathSegments
        var first = ""
        try {
            first=params[0]
        }catch (e:Exception){
            first=""
        }

        if (host.equals(getString(R.string.account_link))){
            if (!first.equals(""))
                accountQr(first)
            else
                onInvalidQr()
            Log.d("CCQR","1")

        }else if(host.equals(getString(R.string.group_invite_host))){
            if (!first.equals("")){
                val intent = Intent(this@QrScanActivity, AcceptInviteLink::class.java)
                intent.data = Uri.parse(value)
                startActivity(intent)
                finish()
            }else{
                onInvalidQr()
                Log.d("CCQR","2")

            }
        }
        else{
            onInvalidQr()
            Log.d("CCQR","3")
        }

    }

    private fun accountQr(number: String) {
        if (!NetworkHelper.isConnected(this@QrScanActivity)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        showProgress()

        try {
            val query = FireConstants.uidByPhone.child(number)
            query.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()){
                        hideProgress()
                        SnackbarUtil.showDoesNotFireAppSnackbar(this@QrScanActivity)
                    }else{
                        val uid = snapshot.getValue(String::class.java)
                        FireConstants.usersRef.child(uid!!).addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                user?.uid = snapshot.value as? String
                                val context = MyApp.context()
                                user?.userName = ContactUtils.queryForNameByNumber(number)
                                user?.isStoredInContacts = ContactUtils.contactExists(context, user?.phone)
                                RealmHelper.getInstance().saveObjectToRealm(user)
                                startChatActivityWithDifferentUser(user!!)
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgress()
                }

            })
        }catch (_:Exception){
            SnackbarUtil.showDoesNotFireAppSnackbar(this@QrScanActivity)
            hideProgress()
        }
    }

    private fun onInvalidQr() {
        Toast.makeText(this, getString(R.string.invalid_account_link), Toast.LENGTH_SHORT).show()

    }
    fun showProgress() {
        dialogBuilder = AlertDialog.Builder(this@QrScanActivity)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.progress_dialog_layout, null)
        dialogBuilder!!.setView(dialogView)
        dialogBuilder!!.setCancelable(false)
        b = dialogBuilder!!.create()
        b!!.show()
    }

    fun hideProgress() {
        b!!.dismiss()
    }

    private fun startChatActivityWithDifferentUser(user: User) {
        hideProgress()
        val intent = Intent(this@QrScanActivity, ChatActivity::class.java)
        intent.putExtra(IntentUtils.UID, user.uid)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}


